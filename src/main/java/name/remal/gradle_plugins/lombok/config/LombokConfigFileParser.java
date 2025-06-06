package name.remal.gradle_plugins.lombok.config;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.lombok.config.LombokConfigNormalizer.normalizeLombokConfigKey;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.CLEAR;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.MINUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.PLUS;
import static name.remal.gradle_plugins.lombok.config.LombokConfigPropertyOperator.SET;
import static name.remal.gradle_plugins.toolkit.PathUtils.getPathLastModifiedIfExists;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringBefore;

import com.google.common.annotations.VisibleForTesting;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.cache.ToolkitCache;
import name.remal.gradle_plugins.toolkit.cache.ToolkitCacheBuilder;

/**
 * See
 * <a href="https://github.com/projectlombok/lombok/blob/master/src/core/lombok/core/configuration/ConfigurationParser.java">https://github.com/projectlombok/lombok/blob/master/src/core/lombok/core/configuration/ConfigurationParser.java</a>
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings({"java:S6395", "java:S3776", "RegExpUnnecessaryNonCapturingGroup"})
abstract class LombokConfigFileParser {

    private static final Pattern NEW_LINE = Pattern.compile("(?:\\r\\n)|(?:\\n\\r)|(?:\\n)|(?:\\r)");

    private static final Pattern PROPERTY = Pattern.compile("(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*))");
    private static final Pattern IMPORT = Pattern.compile("import\\s+(.+)");


    private static final ToolkitCache<LombokConfigPath, LombokConfigFile> PARSE_CACHE =
        new ToolkitCacheBuilder<LombokConfigPath, LombokConfigFile>()
            .withLastModifiedTimeGetter(key -> getPathLastModifiedIfExists(key.getFileSystemPath()))
            .withLoader(LombokConfigFileParser::parseLombokConfigFileImpl)
            .build();

    public static LombokConfigFile parseLombokConfigFile(LombokConfigPath lombokConfigPath) {
        return PARSE_CACHE.get(lombokConfigPath);
    }


    @VisibleForTesting
    @SuppressWarnings("StringSplitter")
    static LombokConfigFile parseLombokConfigFileImpl(LombokConfigPath file) {
        var lombokConfigFileBuilder = LombokConfigFile.builder().file(file);

        var content = file.readContent();
        var lines = NEW_LINE.split(content);
        for (int lineNumber = 1; lineNumber <= lines.length; ++lineNumber) {
            String originalLine = lines[lineNumber - 1];
            var line = substringBefore(originalLine, "#").trim();
            if (line.isEmpty()) {
                continue;
            }

            var importMatcher = IMPORT.matcher(line);
            if (importMatcher.matches()) {
                var importInstruction = ImportInstruction.builder()
                    .file(file)
                    .lineNumber(lineNumber)
                    .value(importMatcher.group(1))
                    .build();
                lombokConfigFileBuilder.importInstruction(importInstruction);
                continue;
            }

            var propertyMatcher = PROPERTY.matcher(line);
            if (propertyMatcher.matches()) {
                if (propertyMatcher.group(1) == null) {
                    var key = normalizeLombokConfigKey(propertyMatcher.group(2));
                    var operatorString = propertyMatcher.group(3);
                    var value = propertyMatcher.group(4);

                    final LombokConfigPropertyOperator operator;
                    switch (operatorString) {
                        case "=":
                            operator = SET;
                            break;
                        case "+=":
                            operator = PLUS;
                            break;
                        case "-=":
                            operator = MINUS;
                            break;
                        default:
                            lombokConfigFileBuilder.parseError(LombokConfigFileParseError.builder()
                                .file(file)
                                .lineNumber(lineNumber)
                                .message("Invalid line (unsupported operator): " + line)
                                .build()
                            );
                            continue;
                    }

                    lombokConfigFileBuilder.property(LombokConfigFileProperty.builder()
                        .key(key)
                        .operator(operator)
                        .value(value)
                        .file(file)
                        .lineNumber(lineNumber)
                        .build()
                    );

                } else {
                    var key = normalizeLombokConfigKey(propertyMatcher.group(1));
                    lombokConfigFileBuilder.property(LombokConfigFileProperty.builder()
                        .key(key)
                        .operator(CLEAR)
                        .value("")
                        .file(file)
                        .lineNumber(lineNumber)
                        .build()
                    );
                }

                continue;
            }

            lombokConfigFileBuilder.parseError(LombokConfigFileParseError.builder()
                .file(file)
                .lineNumber(lineNumber)
                .message("Invalid line: " + line)
                .build()
            );
        }

        return lombokConfigFileBuilder.build();
    }

}
