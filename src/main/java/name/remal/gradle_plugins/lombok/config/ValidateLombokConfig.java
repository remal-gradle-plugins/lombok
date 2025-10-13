package name.remal.gradle_plugins.lombok.config;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static name.remal.gradle_plugins.lombok.config.LombokConfigUtils.parseLombokConfigs;
import static name.remal.gradle_plugins.toolkit.ClosureUtils.configureWith;
import static name.remal.gradle_plugins.toolkit.FileCollectionUtils.finalizeFileCollectionValue;
import static name.remal.gradle_plugins.toolkit.LayoutUtils.getRootPathOf;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyListProxy;
import static name.remal.gradle_plugins.toolkit.ReportContainerUtils.createReportContainerFor;
import static name.remal.gradle_plugins.toolkit.VerificationExceptionUtils.newVerificationException;
import static name.remal.gradle_plugins.toolkit.git.GitUtils.findGitRepositoryRootFor;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import lombok.Getter;
import name.remal.gradle_plugins.lombok.config.rule.LombokConfigRule;
import name.remal.gradle_plugins.lombok.config.rule.LombokConfigValidationContext;
import name.remal.gradle_plugins.toolkit.issues.CheckstyleHtmlIssuesRenderer;
import name.remal.gradle_plugins.toolkit.issues.CheckstyleXmlIssuesRenderer;
import name.remal.gradle_plugins.toolkit.issues.Issue;
import name.remal.gradle_plugins.toolkit.issues.TextIssuesRenderer;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;
import org.intellij.lang.annotations.Language;

@CacheableTask
public abstract class ValidateLombokConfig extends DefaultTask
    implements VerificationTask, Reporting<ValidateLombokConfigReports> {

    private boolean ignoreFailures;

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    @Override
    @Internal
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Getter(onMethod_ = {@Nested})
    private final ValidateLombokConfigReports reports = createReportContainerFor(this);

    @Override
    public ValidateLombokConfigReports reports(@DelegatesTo(strategy = DELEGATE_FIRST) Closure closure) {
        configureWith(reports, closure);
        return reports;
    }

    @Override
    public ValidateLombokConfigReports reports(Action<? super ValidateLombokConfigReports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

    @Internal
    public abstract ConfigurableFileCollection getDirectories();

    private transient final List<LombokConfig> lombokConfigs = asLazyListProxy(() -> {
        var directories = getDirectories();
        finalizeFileCollectionValue(directories);
        return parseLombokConfigs(directories.getFiles());
    });

    @InputFiles
    @PathSensitive(RELATIVE)
    @org.gradle.api.tasks.Optional
    protected abstract ConfigurableFileCollection getInvolvedPaths();

    {
        getInvolvedPaths().from(getProject().provider(() -> lombokConfigs.stream()
            .map(LombokConfig::getInvolvedPaths)
            .flatMap(Collection::stream)
            .collect(toCollection(LinkedHashSet::new))));
    }

    @Input
    @org.gradle.api.tasks.Optional
    public abstract SetProperty<String> getDisabledRules();

    @TaskAction
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    public void execute() {
        var disabledRules = getDisabledRules().get();

        var rules = stream(ServiceLoader.load(LombokConfigRule.class, LombokConfigRule.class.getClassLoader())
            .spliterator(), false).filter(not(rule -> disabledRules.contains(rule.getName())))
            .filter(not(rule -> rule.getAliases().stream().anyMatch(disabledRules::contains)))
            .collect(toList());


        var context = new Context();
        for (var lombokConfig : lombokConfigs) {
            for (var rule : rules) {
                rule.validate(lombokConfig, context);
            }
        }


        var issues = context.getIssues();

        var xmlReportLocation = getReports().getXml().getOutputLocation().getAsFile().getOrNull();
        if (xmlReportLocation != null) {
            new CheckstyleXmlIssuesRenderer().renderIssuesToFile(issues, xmlReportLocation);
        }

        var htmlReportLocation = getReports().getHtml().getOutputLocation().getAsFile().getOrNull();
        if (htmlReportLocation != null) {
            new CheckstyleHtmlIssuesRenderer("Lombok Config").renderIssuesToFile(issues, htmlReportLocation);
        }

        if (!issues.isEmpty()) {
            getLogger().error(new TextIssuesRenderer().renderIssues(issues));

            if (!getIgnoreFailures()) {
                throw newVerificationException(format("Lombok config validation analysis failed with %d issues",
                    issues.size()));
            }
        }
    }


    @Internal
    protected abstract DirectoryProperty getRootDir();

    {
        var project = getProject();
        getRootDir().set(project.getLayout().dir(project.provider(() -> {
            var rootProjectDir = getRootPathOf(project);
            var gitRootDir = findGitRepositoryRootFor(rootProjectDir);
            var rootDir = gitRootDir != null ? gitRootDir : rootProjectDir;
            return rootDir.toFile();
        })));
    }

    @Getter
    private class Context implements LombokConfigValidationContext {

        private final Collection<Issue> issues = new LinkedHashSet<>();

        @Override
        public Path getRootPath() {
            return getRootDir().getAsFile().get().toPath();
        }

        @Override
        public void report(String rule, Path path, @Nullable Integer lineNumber, @Language("TEXT") String message) {
            issues.add(newIssueBuilder().sourceFile(path.toFile())
                .message(textMessageOf(message))
                .rule(rule)
                .startLine(lineNumber)
                .build()
            );
        }

    }

}
