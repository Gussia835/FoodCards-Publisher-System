package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.FileReceiverService;
import org.example.utils.Constants;
import org.example.utils.filename.FileManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanController {
    private final FileReceiverService service;
    private final FileManager fileManager;

    @Scheduled(cron = "${scheduler.cleanup.start:0 0 2 * * ?}")
    public void cleanUpDirectory() {

        log.info("directory clean some folders");
        try {
            int deletedCount = fileManager.deleteOldDirectories();
            log.info("Cleanup successful. Deleted {} old directories.", deletedCount);
        } catch (Exception e) {
            log.error("cant delete old directories", e);
        }
    }

    @Scheduled(fixedDelayString = "${scheduler.scan.frequency:10000}")
    public void scanDirectory() {
        log.info("directory is scanning right now");
        Path processDir = fileManager.getProcessPath();

        if (!Files.exists(processDir)) {
            log.warn("directory is not exist scanner dont process file");
            return;
        }

        try (Stream<Path> paths = Files.list(processDir)) {
            paths.filter(path -> {
                String name = path.getFileName().toString();

                log.info("directory find file: {}", name);
                return !name.endsWith(Constants.EXT_SUCCESS) &&
                        !name.endsWith(Constants.EXT_ERROR) &&
                        !name.endsWith(Constants.EXT_IN_PROGRESS);
            }).filter(Files::isRegularFile)
                    .forEach(path -> {
                try {
                    log.info("get local file: {}", path.getFileName());
                    service.processFile(path);
                } catch (Exception e) {
                    log.error("Failed to process local file: {}", path, e);
                }
            });
        } catch (IOException e) {
            log.error("Failed to read directory: {}", processDir, e);
        }
    }
}
