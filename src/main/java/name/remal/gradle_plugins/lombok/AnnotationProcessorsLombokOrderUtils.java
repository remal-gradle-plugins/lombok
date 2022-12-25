package name.remal.gradle_plugins.lombok;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class AnnotationProcessorsLombokOrderUtils {

    private static final Pattern LOMBOK_FILE_NAME = Pattern.compile("lombok(-\\d+.*)?\\.jar");
    private static final Pattern LOMBOK_RELATED_FILE_NAME = Pattern.compile(".*\\blombok\\b.*", CASE_INSENSITIVE);

    private static final Pattern LOMBOK_PROCESSOR = Pattern.compile("lombok\\..*");
    private static final Pattern LOMBOK_RELATED_PROCESSOR = Pattern.compile(".*\\blombok\\b.*", CASE_INSENSITIVE);


    public static List<File> withFixedAnnotationProcessorFilesOrder(@Nullable Collection<File> files) {
        if (isEmpty(files)) {
            return emptyList();
        }

        val filesList = new ArrayList<>(files);
        filesList.sort(comparing(AnnotationProcessorsLombokOrderUtils::getFileOrder));
        return unmodifiableList(filesList);
    }

    private static int getFileOrder(File file) {
        val fileName = file.getName();

        if (LOMBOK_FILE_NAME.matcher(fileName).matches()) {
            return 100;
        }

        if (LOMBOK_RELATED_FILE_NAME.matcher(fileName).matches()) {
            return 10;
        }

        return 0;
    }


    public static List<String> withFixedAnnotationProcessorsOrder(@Nullable Collection<String> processors) {
        if (isEmpty(processors)) {
            return emptyList();
        }

        val processorsList = new ArrayList<>(processors);
        processorsList.sort(comparing(AnnotationProcessorsLombokOrderUtils::getProcessorOrder));
        return unmodifiableList(processorsList);
    }

    private static int getProcessorOrder(String processor) {
        if (LOMBOK_PROCESSOR.matcher(processor).matches()) {
            return 100;
        }

        if (LOMBOK_RELATED_PROCESSOR.matcher(processor).matches()) {
            return 10;
        }

        return 0;
    }

}
