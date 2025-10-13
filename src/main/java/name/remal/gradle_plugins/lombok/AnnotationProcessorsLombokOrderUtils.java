package name.remal.gradle_plugins.lombok;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
abstract class AnnotationProcessorsLombokOrderUtils {

    private static final Map<Pattern, Integer> FILE_NAME_ORDERS = ImmutableMap.<Pattern, Integer>builder()
        .put(Pattern.compile("mapstruct-processor(-\\d+.*)?\\.jar"), -100)
        .put(Pattern.compile(".*\\bmapstruct\\b.*"), -99)
        .put(Pattern.compile("lombok(-\\d+.*)?\\.jar"), 0)
        .put(Pattern.compile(".*\\blombok\\b.*"), 1)
        .build();

    private static final Map<Pattern, Integer> PROCESSOR_ORDERS = ImmutableMap.<Pattern, Integer>builder()
        .put(Pattern.compile("org\\.mapstruct\\..+"), -100)
        .put(Pattern.compile(".*\\bmapstruct\\b.*"), -99)
        .put(Pattern.compile("lombok\\..+"), 0)
        .put(Pattern.compile(".*\\blombok\\b.*"), 1)
        .build();


    public static List<File> withFixedAnnotationProcessorFilesOrder(@Nullable Collection<File> files) {
        if (isEmpty(files)) {
            return emptyList();
        }

        var filesList = new ArrayList<>(files);
        filesList.sort(comparing(AnnotationProcessorsLombokOrderUtils::getFileOrder));
        return unmodifiableList(filesList);
    }

    private static int getFileOrder(File file) {
        var fileName = file.getName();

        for (var entry : FILE_NAME_ORDERS.entrySet()) {
            if (entry.getKey().matcher(fileName).matches()) {
                return entry.getValue();
            }
        }

        return Integer.MAX_VALUE;
    }


    public static List<String> withFixedAnnotationProcessorsOrder(@Nullable Collection<String> processors) {
        if (isEmpty(processors)) {
            return emptyList();
        }

        var processorsList = new ArrayList<>(processors);
        processorsList.sort(comparing(AnnotationProcessorsLombokOrderUtils::getProcessorOrder));
        return unmodifiableList(processorsList);
    }

    private static int getProcessorOrder(String processor) {
        for (var entry : PROCESSOR_ORDERS.entrySet()) {
            if (entry.getKey().matcher(processor).matches()) {
                return entry.getValue();
            }
        }

        return Integer.MAX_VALUE;
    }

}
