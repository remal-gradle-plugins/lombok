package name.remal.gradleplugins.lombok;

import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorFilesOrder;
import static name.remal.gradleplugins.lombok.AnnotationProcessorsLombokOrderUtils.withFixedAnnotationProcessorsOrder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AnnotationProcessorsLombokOrderUtilsTest {

    @Nested
    class FixAnnotationProcessorFilesOrder {

        @Test
        void noLombok() {
            val files = Stream.of(
                "/root/zz.jar",
                "/root/aa.jar"
            ).map(File::new).collect(toUnmodifiableList());

            val fixedFiles = withFixedAnnotationProcessorFilesOrder(files);

            assertThat(fixedFiles).containsExactly(Stream.of(
                "/root/zz.jar",
                "/root/aa.jar"
            ).map(File::new).toArray(File[]::new));
        }

        @Test
        void withLombok() {
            val files = Stream.of(
                "/root/lombok-1.jar",
                "/root/zz.jar",
                "/root/aa.jar"
            ).map(File::new).collect(toUnmodifiableList());

            val fixedFiles = withFixedAnnotationProcessorFilesOrder(files);

            assertThat(fixedFiles).containsExactly(Stream.of(
                "/root/zz.jar",
                "/root/aa.jar",
                "/root/lombok-1.jar"
            ).map(File::new).toArray(File[]::new));
        }

        @Test
        void withLombokWithoutVersion() {
            val files = Stream.of(
                "/root/lombok.jar",
                "/root/zz.jar",
                "/root/aa.jar"
            ).map(File::new).collect(toUnmodifiableList());

            val fixedFiles = withFixedAnnotationProcessorFilesOrder(files);

            assertThat(fixedFiles).containsExactly(Stream.of(
                "/root/zz.jar",
                "/root/aa.jar",
                "/root/lombok.jar"
            ).map(File::new).toArray(File[]::new));
        }

    }


    @Nested
    class FixAnnotationProcessorsOrder {

        @Test
        void noLombok() {
            val processors = Stream.of(
                "some.package.Z",
                "some.package.A"
            ).collect(toUnmodifiableList());

            val fixedProcessors = withFixedAnnotationProcessorsOrder(processors);

            assertThat(fixedProcessors).containsExactly(Stream.of(
                "some.package.Z",
                "some.package.A"
            ).toArray(String[]::new));
        }

        @Test
        void withLombok() {
            val processors = Stream.of(
                "lombok.Processor",
                "some.package.Z",
                "some.package.A"
            ).collect(toUnmodifiableList());

            val fixedProcessors = withFixedAnnotationProcessorsOrder(processors);

            assertThat(fixedProcessors).containsExactly(Stream.of(
                "some.package.Z",
                "some.package.A",
                "lombok.Processor"
            ).toArray(String[]::new));
        }

    }

}
