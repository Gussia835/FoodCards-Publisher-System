package org.example.service.impl.visitor.validator;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.exception.ReceivingFileException;
import org.example.utils.Constants;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@Component
@Slf4j
public class EnrollValidator {

    /**
    Проверяет первую строку (Header) на валидность.
    */
    public boolean validateHeader(Path filepath) {
        try {
            String firstLine = readFirstLine(filepath);
            return isValidLen(firstLine, Constants.LEN_HEADER)
                    && isValidPattern(firstLine, Constants.REGEX_HEADER)
                    && isValidProcTypeHeader(firstLine);
        } catch (IOException e) {
            log.error("cant read header");
            return false;
        }
    }

    /**
     Вспомогательная: Читает первую строку файла
     */
    private String readFirstLine(Path filepath) throws IOException{
        try (var reader = Files.newBufferedReader(filepath, Constants.CHARSET_WINDOWS_1251)) {
            String firstLine = reader.readLine();
            log.info("read header: '{}', length: {}", firstLine, firstLine != null ? firstLine.length() : 0);

            return firstLine;
        }
    }

    /**
     Вспомогательная: Проверяет процедуру из хедера на валдиность
     */
    private boolean isValidProcTypeHeader(String line) {
        String procType = line.substring(Constants.IDX_PROC_START, Constants.IDX_PROC_END).trim();
        if (!Constants.REGEX_PROC_TYPE.matcher(procType).matches()) {
            log.warn("Invalid PROC_TYPE: {}", procType);
            return false;
        }
        return true;
    }

    /**
     Проверяет имя файла на валидность
     */
    public boolean isValidFilename(Path filepath) {
        if (filepath == null) {
            return false;
        }
        String filename = filepath.getFileName().toString();
        return Constants.REGEX_FILENAME.matcher(filename).matches();
    }

    /**
     Проверяет последнюю строку railer а валидность
     */
    public boolean validateTrailer(Path filepath) {
        try {
            String lastLine = readLastLine(filepath);
            return isValidLen(lastLine, Constants.LEN_TRAILER)
                    && isValidPattern(lastLine, Constants.REGEX_TRAILER)
                    && isValidCount(lastLine, filepath);
        } catch (IOException e) {
            log.error("Failed to read trailer", e);
            return false;
        }
    }

    /**
     Вспомогательная: читает строку последнюю trailer
     */
    private String readLastLine(Path filepath) throws IOException {
        File file = filepath.toFile();
        long fileLength = file.length();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long startPos = fileLength - Constants.LINE_SIZE_TRAILER;
            raf.seek(startPos);
            byte[] bytes = new byte[Constants.LEN_TRAILER];
            raf.readFully(bytes);
            return new String(bytes, Constants.CHARSET_WINDOWS_1251);
        }
    }

    /**
     Вспомогательная: проверяет валидность числа в trailer
     */
    private boolean isValidCount(String lastLine, Path filepath) {
        String countStr = lastLine.substring(Constants.IDX_COUNT_START, Constants.IDX_COUNT_END).trim();
        int fileCount = Integer.parseInt(countStr);
        int actualCount = countLines(filepath);
        log.info("line: {}, count: {}", lastLine, actualCount);

        if (fileCount != actualCount) {
            log.warn("Trailer count mismatch. Declared: {}, Actual: {}", fileCount, actualCount);
            return false;
        }
        return true;
    }

    /**
     Вспомогательная: считает строки в файле
     */
    private int countLines(Path filepath) {
        try {
            long fileLength = Files.size(filepath);
            long dataBytes = fileLength - (Constants.LINE_SIZE_HEADER + Constants.LINE_SIZE_TRAILER);
            log.debug("fileLength: {}, dataBytes: {}, calculated lines: {}", fileLength, dataBytes, (int) dataBytes / Constants.LINE_SIZE_BODY);

            return (int) (dataBytes / Constants.LINE_SIZE_BODY);
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filepath.getFileName(), e);
            throw new ReceivingFileException(filepath.getFileName().toString(), "get size of");
        }
    }

    /**
     Вспомогательная: проверяет длину на валидность
     */
    private boolean isValidLen(String line, int len) {
        if (line == null || line.length() < len) {
            log.warn("Line is missing or too short. Expected: {}, Actual: {}", len, line != null ? line.length() : 0);
            return false;
        }

        return true;
    }


    /**
     Проверяет строку
     */
    public boolean validateBody(String line) {
        return isValidLen(line, Constants.LEN_BODY)
                && isValidPattern(line, Constants.REGEX_BODY)
                && isValidOp(line)
                && isValidAccount(line);
    }

    /**
     Вспомогательная: проверяет формат строки на валдиность по паттерну
     */
    private boolean isValidPattern(String line, Pattern pattern) {
        if (!pattern.matcher(line).matches()) {
            log.warn("Line does not match expected pattern");
            return false;
        }
        return true;
    }

    /**
     Вспомогательная: проверяет поле аккаунта из body на валдиность
     */
    private boolean isValidAccount(String line) {
        String account = line.substring(Constants.IDX_ACC_START, Constants.IDX_ACC_END).trim();
        return StringUtils.isNotBlank(account)
                && Constants.REGEX_NUMERIC.matcher(account).matches();
    }

    /**
     Вспомогательная: проверяет поле операции из body на валдиность
     */
    private boolean isValidOp(String line) {
        String opType = line.substring(Constants.IDX_TYPE_START, Constants.IDX_TYPE_END).trim();
        return Constants.OP_DR.equals(opType)
                || Constants.OP_CR.equals(opType)
                || Constants.OP_ZR.equals(opType);
    }

    /**
     проверяет является ли строка последней - trailer
     */
    public boolean isTrailerLine(String line) {
        return line != null
                && line.length() == Constants.LEN_TRAILER
                && Constants.REGEX_TRAILER.matcher(line).matches();
    }

    /**
     проверяет является ли строка первой - header
     */
    public boolean isHeaderLine(String line) {
        return line != null
                && line.length() >= Constants.LEN_HEADER
                && line.startsWith("H ");
    }
}
