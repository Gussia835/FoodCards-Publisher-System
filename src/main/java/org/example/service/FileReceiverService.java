package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.models.pom.PomFile;
import org.example.service.dao.SaverDAO;
import org.example.service.impl.visitor.EnrollParserVisitor;
import org.example.service.impl.visitor.validator.EnrollValidator;
import org.example.utils.Constants;
import org.example.utils.filename.FileManager;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileReceiverService {
    private final EnrollParserVisitor parserVisitor;
    private final FileManager fileManager;
    private final SaverDAO saver;
    private final EnrollValidator validator;

    /**
     Обработка файла через визитор

     @param filepath Путь к новому файлу в директории process
     */
    public void processFile(Path filepath) {
        String filename = filepath.getFileName().toString();
        Path inProgressPath = fileManager.moveToInProgress(filepath);
        log.info("filename: {} {}", filename, filename.length());

        String uliDate = filename.substring(filename.length() - 3);
        PomFile pomFile = PomFile.builder()
                .filename(filename)
                .fullPath(inProgressPath.getParent().toString())
                .fileStatus(Constants.STATUS_IN_PROGRESS)
                .uliDate(uliDate)
                .build();
        pomFile = saver.savePomFile(pomFile);

        boolean isValidHeader = validator.validateHeader(inProgressPath);
        boolean isValidTrailer = validator.validateTrailer(inProgressPath);
        if (!isValidHeader || !isValidTrailer) {
            log.warn("file is not valid header is valid: {}, trailer is valid: {}", isValidHeader, isValidTrailer);
        }
        parserVisitor.setContext(pomFile.getId(), isValidHeader, isValidTrailer);

        try (BufferedReader reader = Files.newBufferedReader(inProgressPath, Constants.CHARSET_WINDOWS_1251)) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("processing line {}", line);
                parserVisitor.visit(line);
            }
            pomFile.setFileStatus(isValidTrailer && isValidHeader ? Constants.STATUS_SUCCESS : Constants.STATUS_ERROR);

            saver.savePomFile(pomFile);
            log.debug("moving to file result: result: {}", isValidHeader && isValidTrailer);
            fileManager.moveToFileResult(inProgressPath, isValidHeader && isValidTrailer);

        } catch (Exception e) {
            log.error("exception while file processing {}", filename, e);
            pomFile.setFileStatus(Constants.STATUS_ERROR);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            if (errorMsg.length() > Constants.MAX_ERROR_MSG_LENGTH) {
                errorMsg = errorMsg.substring(0, Constants.MAX_ERROR_MSG_LENGTH-3) + "...";
            }
            pomFile.setFileComment(errorMsg);

            saver.savePomFile(pomFile);
            fileManager.moveToFileResult(inProgressPath, false);

            throw new RuntimeException("Failed to process file: " + filename, e);
        }
    }
}
