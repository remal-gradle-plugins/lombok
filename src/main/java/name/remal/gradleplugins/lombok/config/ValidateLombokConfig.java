package name.remal.gradleplugins.lombok.config;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static name.remal.gradleplugins.lombok.config.LombokConfigUtils.parseLombokConfigs;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.ReportContainerUtils.createReportContainerFor;
import static name.remal.gradleplugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradleplugins.toolkit.issues.TextMessage.textMessageOf;
import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import name.remal.gradleplugins.lombok.config.rule.LombokConfigRule;
import name.remal.gradleplugins.lombok.config.rule.LombokConfigValidationContext;
import name.remal.gradleplugins.toolkit.issues.CheckstyleHtmlIssuesRenderer;
import name.remal.gradleplugins.toolkit.issues.CheckstyleXmlIssuesRenderer;
import name.remal.gradleplugins.toolkit.issues.Issue;
import name.remal.gradleplugins.toolkit.issues.TextIssuesRenderer;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
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
@Setter
public abstract class ValidateLombokConfig
    extends DefaultTask
    implements VerificationTask, Reporting<ValidateLombokConfigReports> {

    private boolean ignoreFailures;

    @Override
    @Internal
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Getter(onMethod_ = {@Nested})
    private final ValidateLombokConfigReports reports = createReportContainerFor(this);

    @Override
    public ValidateLombokConfigReports reports(@DelegatesTo(strategy = DELEGATE_FIRST) Closure closure) {
        getProject().configure(reports, closure);
        return reports;
    }

    @Override
    public ValidateLombokConfigReports reports(Action<? super ValidateLombokConfigReports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

    @Internal
    public abstract ConfigurableFileCollection getDirectories();

    @Internal
    protected abstract ListProperty<LombokConfig> getLombokConfigs();

    {
        getLombokConfigs().addAll(getProject().provider(() ->
            parseLombokConfigs(getDirectories().getFiles())
        ));
    }

    @InputFiles
    @PathSensitive(RELATIVE)
    @org.gradle.api.tasks.Optional
    protected abstract ConfigurableFileCollection getInvolvedPaths();

    {
        getInvolvedPaths().from(getProject().provider(() ->
            getLombokConfigs().get().stream()
                .map(LombokConfig::getInvolvedPaths)
                .flatMap(Collection::stream)
                .collect(toSet())
        ));
    }

    @Input
    @org.gradle.api.tasks.Optional
    public abstract SetProperty<String> getDisabledRules();

    @TaskAction
    public void execute() {
        val lombokConfigs = getLombokConfigs().get();

        val disabledRules = getDisabledRules().get();

        val rules = stream(
            ServiceLoader.load(LombokConfigRule.class, LombokConfigRule.class.getClassLoader()).spliterator(),
            false
        )
            .filter(not(rule -> disabledRules.contains(rule.getName())))
            .filter(not(rule -> rule.getAliases().stream().anyMatch(disabledRules::contains)))
            .collect(toList());


        val context = new Context();
        for (val lombokConfig : lombokConfigs) {
            for (val rule : rules) {
                rule.validate(lombokConfig, context);
            }
        }


        val issues = context.getIssues();

        val xmlReportLocation = getReports().getXml().getOutputLocation().getAsFile().getOrNull();
        if (xmlReportLocation != null) {
            new CheckstyleXmlIssuesRenderer().renderIssuesToFile(issues, xmlReportLocation);
        }

        val htmlReportLocation = getReports().getHtml().getOutputLocation().getAsFile().getOrNull();
        if (htmlReportLocation != null) {
            new CheckstyleHtmlIssuesRenderer().renderIssuesToFile(issues, htmlReportLocation);
        }

        if (!issues.isEmpty()) {
            getLogger().error(new TextIssuesRenderer().renderIssues(issues));

            if (!getIgnoreFailures()) {
                throw new AssertionError(format(
                    "Lombok config validation analysis failed with %d issues",
                    issues.size()
                ));
            }
        }
    }

    @Getter
    private class Context implements LombokConfigValidationContext {

        private final Collection<Issue> issues = new LinkedHashSet<>();

        @Override
        public Project getProject() {
            return ValidateLombokConfig.this.getProject();
        }

        @Override
        public void report(Path path, @Nullable Integer lineNumber, @Language("TEXT") String message) {
            issues.add(newIssueBuilder()
                .sourceFile(path.toFile())
                .message(textMessageOf(message))
                .startLine(lineNumber)
                .build()
            );
        }

    }

}
