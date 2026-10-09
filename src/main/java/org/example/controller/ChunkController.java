package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.utils.Constants;
import org.example.utils.filename.FileManager;
import org.example.service.FileReceiverService;
import org.example.service.impl.visitor.validator.EnrollValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class ChunkController {
    private final FileReceiverService service;
    private final FileManager fileManager;
    private final EnrollValidator validator;

    @PostMapping("/chunk")
    public ResponseEntity<String> getChunk(@RequestHeader("filename") String filename,
                                           InputStream requestStream
                                           ) {
        log.info("Received chunked stream for file: {}", filename);

        if (!validator.isValidFilename(Paths.get(filename))) {
            log.error("Chunk upload rejected. {}: {}", Constants.MSG_INVALID_FILENAME, filename);
            return ResponseEntity.badRequest().body(Constants.MSG_INVALID_FILENAME);
        }
        Path filepath = fileManager.saveChunk(filename, requestStream);
        service.processFile(filepath);

        return ResponseEntity.ok(Constants.MSG_UPLOAD_SUCCESS);
    }
}
