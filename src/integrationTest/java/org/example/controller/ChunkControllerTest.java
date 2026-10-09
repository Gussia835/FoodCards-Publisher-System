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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChunkController.class)
class ChunkControllerTest {

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

    private static final String ENDPOINT = "/files/chunk";
    private static final String VALID_FILENAME = "Z001032.GLAER_ENROLL0010321.249";
    private static final String INVALID_FILENAME = "invalid.txt";
    private static final byte[] CHUNK_DATA = "chunk data content".getBytes();

    @Nested
    class SuccessTests {

        @Test
        void validChunkedStreamTest() throws Exception {
            Path inProgressPath = tempDir.resolve("in_progress/" + VALID_FILENAME);
            when(validator.isValidFilename(any())).thenReturn(true);
            when(fileManager.saveChunk(eq(VALID_FILENAME), any())).thenReturn(inProgressPath);
            doNothing().when(service).processFile(any(Path.class));

            mockMvc.perform(post(ENDPOINT)
                            .header("filename", VALID_FILENAME)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .content(CHUNK_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().string("File fully uploaded and processing started"));
            verify(service, times(1)).processFile(inProgressPath);
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void invalidFilenameTest() throws Exception {
            when(validator.isValidFilename(any())).thenReturn(false);
            mockMvc.perform(post(ENDPOINT)
                            .header("filename", INVALID_FILENAME)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .content(CHUNK_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid filename format"));
            verify(service, never()).processFile(any());
        }

        @Test
        void missingFilenameHeaderTest() throws Exception {
            mockMvc.perform(post(ENDPOINT)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .content(CHUNK_DATA))
                    .andExpect(status().isBadRequest());
            verify(service, never()).processFile(any());
        }
    }
}