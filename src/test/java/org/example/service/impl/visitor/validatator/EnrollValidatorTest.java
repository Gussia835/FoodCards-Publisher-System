package org.example.service.impl.visitor.validatator;

import org.example.service.impl.visitor.validator.EnrollValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class EnrollValidatorTest {

    private final EnrollValidator validator = new EnrollValidator();

    @TempDir
    private Path dir;

    private static final Charset WIN_1251 = Charset.forName("windows-1251");

    @Nested
    class HeaderValidatorTest {
        @Test
        void validateImmediateHeaderTest() throws IOException {

            String headerLine = "H 20260907 182606 IMMEDIATE               \r\n";
            Path file = dir.resolve("header.txt");
            Files.writeString(file, headerLine, WIN_1251);

            boolean res = validator.validateHeader(file);
            assertThat(res).isTrue();
        }

        @Test
        void validateInTimeHeaderTest() throws IOException {

            String headerLine = "H 20260907 182606 IN-TIME  20260811 235900\r\n";
            Path file = dir.resolve("header.txt");
            Files.writeString(file, headerLine, WIN_1251);

            boolean res = validator.validateHeader(file);
            assertThat(res).isTrue();
        }

        @Test
        void invalidHeaderFormatTest() throws IOException {
            String header = "X 20260907 182606 IMMEDIATE               \r\n";
            Path file = dir.resolve("header.txt");
            Files.writeString(file, header, WIN_1251);

            boolean result = validator.validateHeader(file);
            assertThat(result).isFalse();
        }

        @Test
        void invalidShortHeaderTest() throws IOException {
            String headerLine = "H 20260907\r\n";
            Path file = dir.resolve("header.txt");
            Files.writeString(file, headerLine, WIN_1251);

            boolean res = validator.validateHeader(file);
            assertThat(res).isFalse();
        }

        @Test
        void invalidProcTypeHeaderTest() throws IOException {
            String headerLine = "H 20260907 182606 OO           \r\n";
            Path file = dir.resolve("header.txt");
            Files.writeString(file, headerLine, WIN_1251);

            boolean res = validator.validateHeader(file);
            assertThat(res).isFalse();
        }


    }


    @Nested
    class TrailerValidatorTest {
        @Test
        void validateTrailer() throws IOException {
            String content = "H 20260907 182606 IMMEDIATE               \r\n" +
                    "Кузнецов Олег                                                                                       1000401000050003              DR                 777\r\n" +
                    "T                  1\r\n";
            Path file = dir.resolve("file.txt");
            Files.writeString(file, content, WIN_1251);

            boolean result = validator.validateTrailer(file);
            assertThat(result).isTrue();
        }

        @Test
        void invalidTrailer() throws IOException {
            String content = "H 20260907 182606 IMMEDIATE               \r\n" +
                    "Кузнецов Олег                                                                                       1000401000050003              DR                 777\r\n" +
                    "T                  5\r\n";
            Path file = dir.resolve("file.txt");
            Files.writeString(file, content, WIN_1251);

            boolean result = validator.validateTrailer(file);
            assertThat(result).isFalse();
        }
    }

    @Nested
    class BodyValidatorTest {
        @Test
        void validBodyTest() {
            String line = "Кузнецов Олег                                                                                       1000401000050003              DR                 777";

            boolean result = validator.validateBody(line);
            assertThat(result).isTrue();
        }

        @Test
        void invalidShortBodyTest() {
            String line = "Кузнецов Олег 1000401000050003 DR 777";

            boolean result = validator.validateBody(line);
            assertThat(result).isFalse();
        }

        @Test
        void invalidOpTypeBodyTest() {
            String line = "Кузнецов Олег                                                                                       1000401000050003              XX                 777";

            boolean result = validator.validateBody(line);
            assertThat(result).isFalse();
        }

        @Test
        void invalidAccountBodyTest() {
            String line = "Кузнецов Олег                                                                                       ABC                 DR                 777";

            boolean result = validator.validateBody(line);
            assertThat(result).isFalse();
        }
    }
}
