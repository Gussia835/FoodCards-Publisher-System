package org.example.controller;

import org.example.service.FileReceiverService;
import org.example.service.impl.visitor.validator.EnrollValidator;
import org.example.utils.filename.FileManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MultipartController.class)
class MultipartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileReceiverService service;

    @MockBean
    private FileManager fileManager;

    @MockBean
    private EnrollValidator validator;

    @TempDir
    Path tempDir;

    private static final String ENDPOINT = "/files/multipart";
    private static final Charset WIN_1251 = Charset.forName("windows-1251");
    private static final String VALID_FILENAME = "Z001032.GLAER_ENROLL0010321.249";
    private static final String INVALID_FILENAME = "invalid_name.txt";
    private static final String VALID_CONTENT = "H 20260907 182606 IMMEDIATE               \r\n" +
            "Кузнецов Олег                                                                                       1000401000050003              DR                 777\r\n" +
            "T                  1\r\n";

    @Nested
    class SuccessTests {
        @Test
        void validMultipartFileTest() throws Exception {
            Path tempFile = tempDir.resolve(VALID_FILENAME);
            Files.writeString(tempFile, VALID_CONTENT, WIN_1251);
            Path inProgressPath = tempDir.resolve("in_progress/" + VALID_FILENAME);

            when(validator.isValidFilename(any())).thenReturn(true);
            when(fileManager.writeFile(eq(VALID_FILENAME), any())).thenReturn(inProgressPath);
            doNothing().when(service).processFile(any(Path.class));
            mockMvc.perform(multipart(ENDPOINT)
                            .file(new MockMultipartFile(
                                    "file",
                                    VALID_FILENAME,
                                    "application/octet-stream",
                                    Files.readAllBytes(tempFile)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().string("File processed successfully"));
            verify(service, times(1)).processFile(inProgressPath);
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void emptyFileTest() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    VALID_FILENAME,
                    "application/octet-stream",
                    new byte[0]
            );

            mockMvc.perform(multipart(ENDPOINT).file(emptyFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("File is empty"));
            verify(service, never()).processFile(any());
        }

        @Test
        void invalidFilenameTest() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    INVALID_FILENAME,
                    "application/octet-stream",
                    "content".getBytes()
            );

            when(validator.isValidFilename(any())).thenReturn(false);
            mockMvc.perform(multipart(ENDPOINT).file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid filename format"));
            verify(service, never()).processFile(any());
        }

        @Test
        void wrongHttpMethodTest() throws Exception {
            mockMvc.perform(get(ENDPOINT))
                    .andExpect(status().isMethodNotAllowed());
            verify(service, never()).processFile(any());
        }
    }
}