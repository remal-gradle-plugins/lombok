package name.remal.gradle_plugins.lombok;

import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorFilesOrder;
import static name.remal.gradle_plugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorsOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnnotationProcessorsLombokOrderUtilsTest {

    @ParameterizedTest
    @MethodSource("fileOrderParams")
    void fileOrder(String nameLess, String nameGreater) {
        val expectedFiles = Stream.of(nameLess, nameGreater)
            .map(name -> "/root/" + name)
            .map(File::new)
            .collect(toList());

        val fixedFiles = withFixedAnnotationProcessorFilesOrder(expectedFiles);
        assertThat(fixedFiles).isEqualTo(expectedFiles);


        val reverseFiles = new ArrayList<>(expectedFiles);
        reverse(reverseFiles);

        val fixedReverseFiles = withFixedAnnotationProcessorFilesOrder(reverseFiles);
        assertThat(fixedReverseFiles).isEqualTo(expectedFiles);
    }

    private static Stream<Arguments> fileOrderParams() {
        return Stream.of(
            null,
            arguments("mapstruct-processor-1.jar", "mapstruct-1.jar"),
            arguments("mapstruct-processor-1.jar", "mapstruct"),
            arguments("mapstruct-processor.jar", "mapstruct-1"),
            arguments("mapstruct-processor.jar", "mapstruct.jar"),

            arguments("mapstruct-1", "lombok-1.jar"),
            arguments("mapstruct-1", "lombok.jar"),
            arguments("mapstruct", "lombok-1.jar"),
            arguments("mapstruct", "lombok.jar"),

            arguments("lombok-1.jar", "lombok-1"),
            arguments("lombok-1.jar", "lombok"),
            arguments("lombok.jar", "lombok-1"),
            arguments("lombok.jar", "lombok"),

            arguments("lombok-1", "unknown"),
            arguments("lombok", "unknown"),
            null
        ).filter(Objects::nonNull);
    }


    @ParameterizedTest
    @MethodSource("processorOrderParams")
    void processorOrder(String processorLess, String processorGreater) {
        val expectedProcessors = Stream.of(processorLess, processorGreater)
            .collect(toList());

        val fixedProcessors = withFixedAnnotationProcessorsOrder(expectedProcessors);
        assertThat(fixedProcessors).isEqualTo(expectedProcessors);


        val reverseProcessors = new ArrayList<>(expectedProcessors);
        reverse(reverseProcessors);

        val fixedReverseProcessors = withFixedAnnotationProcessorsOrder(reverseProcessors);
        assertThat(fixedReverseProcessors).isEqualTo(expectedProcessors);
    }

    private static Stream<Arguments> processorOrderParams() {
        return Stream.of(
            null,
            arguments("org.mapstruct.something", "com.something.mapstruct.something"),

            arguments("com.something.mapstruct.something", "lombok.something"),

            arguments("lombok.something", "something.lombok.something"),

            arguments("something.lombok.something", "unknown"),
            null
        ).filter(Objects::nonNull);
    }

}
