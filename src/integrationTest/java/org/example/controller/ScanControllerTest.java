package org.example.controller;

import org.example.service.FileReceiverService;
import org.example.utils.filename.FileManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanControllerTest {
    @Mock
    private FileReceiverService service;

    @Mock
    private FileManager fileManager;

    @InjectMocks
    private ScanController scheduler;

    @TempDir
    Path tempDir;

    private static final String VALID_FILENAME = "Z001032.GLAER_ENROLL0010321.249";

    @Nested
    class SuccessTests {
        @Test
        void processExistingFilesTest() throws Exception {
            Path file = tempDir.resolve(VALID_FILENAME);
            Files.createFile(file);

            when(fileManager.getProcessPath()).thenReturn(tempDir);
            doNothing().when(service).processFile(any(Path.class));
            scheduler.scanDirectory();
            verify(service, times(1)).processFile(file);
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void emptyDirectoryTest() throws Exception {
            when(fileManager.getProcessPath()).thenReturn(tempDir);
            scheduler.scanDirectory();
            verify(service, never()).processFile(any(Path.class));
        }
    }
}