package org.example.utils.filename;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.Properties;
import org.example.exception.FileProcessingException;
import org.example.exception.ReceivingFileException;
import org.example.utils.Constants;
import org.example.utils.enums.FileStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileManager {
    private final Properties properties;

    @Value("${scheduler.cleanup.lifespan:30}")
    private int lifespan;

    /**
     Получение дироектории в которой файлы
     */
    public Path getProcessPath() {
        return Paths.get(properties.getProcessDir());
    }

    /**
     * добавление чанков
     *
     * @param filename имя файла
     * @param chunkData чанк
     * @return вовзращает путь до файла собранного из чанков
     */
    public Path appendChunk(String filename, byte[] chunkData) {
        try {
            Path dir = getProcessPath();
            Files.createDirectories(dir);
            Path filepath = dir.resolve(filename);
            Files.write(filepath, chunkData, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            return filepath;
        } catch (IOException e) {
            log.error("{} for file: {}", Constants.MSG_FAILED_APPEND_CHUNK, filename, e);
            throw new ReceivingFileException(filename, "append gRPC chunk", e);
        }
    }

    /**
     * метод для
     *
     * @param filename - имя файла
     * @param requestStream -
     * @return пуьб до файла
     */
    public Path saveChunk(String filename, InputStream requestStream) {
        try {
            Path dir = getProcessPath();
            Files.createDirectories(dir);
            Path filepath = dir.resolve(filename);
            Files.copy(requestStream, filepath, StandardCopyOption.REPLACE_EXISTING);

            return filepath;
        } catch (IOException e) {
            log.error("{}: {}", Constants.MSG_FAILED_SAVE_CHUNK, filename, e);
            throw new ReceivingFileException(filename, "save chunked stream", e);
        }
    }

    /**
     * получение файла и перенос в processDir
     *
     * @param filename - имя файла
     * @param file - файл
     * @return - путь до сохранненого файла
     */
    public Path writeFile(String filename, MultipartFile file) {
        try {
            Path dir = getProcessPath();
            Files.createDirectories(dir);
            Path filepath = dir.resolve(filename);
            file.transferTo(filepath.toFile());

            return filepath;
        } catch (IOException e) {
            throw new ReceivingFileException(filename, "write multipart file", e);
        }
    }

    /**
     * перепос файла в папку файлов где обрабатываются файлы
     *
     * @param sourcePath - путь до файла в директории хранения
     * @return путь до файла в папке in-progress
     */
    public Path moveToInProgress(Path sourcePath) {
        return moveToStatus(sourcePath, FileStatus.IN_PROGRESS);
    }

    /**
     * перепос файла в папку файлов где хранится файл с готовым статусом (success/error)
     *
     * @param inProgressPath - путь до файла в директории обрабатывающихся файлов
     * @param isSuccess - статус успешный ли файл
     */
    public void moveToFileResult(Path inProgressPath, boolean isSuccess) {
        FileStatus status = isSuccess ? FileStatus.SUCCESS : FileStatus.ERROR;
        moveToStatus(inProgressPath, status);
    }

    /**
     * перепос файла в папку файлов где хранится файл с готовым статусом (success/error) и изменение арсширения
     *
     * @param filepath - путь до файла в директории обрабатывающихся файлов
     * @param status - статус успешный ли файл
     * @return путь до файла в папке success/error
     */
    private Path moveToStatus(Path filepath, FileStatus status) {
        try {
            Path target = getCurrDir(status);
            Files.createDirectories(target);

            String filename = filepath.getFileName().toString();
            String newFilename = FileNaemUtils.replaceExtension(filename, status);
            Path targetFile = target.resolve(newFilename);
            Files.move(filepath, targetFile, StandardCopyOption.REPLACE_EXISTING);

            return targetFile;
        } catch (IOException e) {
            throw new FileProcessingException(filepath.getFileName().toString(), Constants.MSG_FAILED_MOVE_FILE);
        }
    }

    /**
     * создание директории в зависимости от статуса
     *
     * @param status статус файла
     * @return путь до созданной директории
     */
    private Path getCurrDir(FileStatus status) {
        String dir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return Path.of(properties.getTargetDir(), dir, status.getFolderName());
    }

    /**
     * удаление устаревших файлов и директорий
     *
     * @return количество удаленных директорий
     */
    public int deleteOldDirectories() throws IOException {
        Path path = getProcessPath();
        if (!Files.exists(path)) {
            log.warn("Process directory does not exist: {}", path);
            return 0;
        }
        Instant timeOfOld = Instant.now().minus(lifespan, ChronoUnit.DAYS);

        try (Stream<Path> oldDirs = Files.list(path)) {
            return oldDirs
                    .filter(Files::isDirectory)
                    .filter(dir -> isOld(dir, timeOfOld))
                    .mapToInt(this::deleteDir)
                    .sum();
        }
    }

    /**
     * проверка на старость файла
     *
     * @param dir - директория в которой нужно проверить файлы
     * @param timeOfOld - время после которого файл устаревает
     * @return true если файл устаревший false - иначе
     */
    private boolean isOld(Path dir, Instant timeOfOld) {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(dir);

            return lastModifiedTime.toInstant().isBefore(timeOfOld);

        } catch (IOException e) {

            log.error("cant get last modified time for {}", dir, e);
            return false;
        }
    }

    /**
     * Вспомогательная  удаление директории с её внутренними файлами и папками
     *
     */
    private int deleteDir(Path dir) {
        try {
            boolean isDelete = FileSystemUtils.deleteRecursively(dir);
            if (!isDelete) {
                log.warn("deleting isnt success: {}", dir);
                return 0;
            } else {
                log.info("file was deleted {}", dir);
                return 1;
            }
        } catch (IOException e) {
            log.error("cant delete dir: {}", dir, e);
            return 0;
        }
    }
}
