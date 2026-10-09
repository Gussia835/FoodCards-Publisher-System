package org.example.utils;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Constants {

    // Статусы
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_IN_PROGRESS = "IN_PROCESS";
    public static final String STATUS_WAIT = "WAIT";

    // Расширение
    public static final String EXT_SUCCESS = ".success";
    public static final String EXT_ERROR = ".error";
    public static final String EXT_IN_PROGRESS = ".in_progress";

    // Назване папок для распредения по статусу
    public static final String FOLDER_SUCCESS = "success";
    public static final String FOLDER_ERROR = "error";
    public static final String FOLDER_IN_PROGRESS = "in_progress";

    // Типы Pom Unit
    public static final String POM_TYPE_HEADER = "101";
    public static final String POM_TYPE_BODY = "106";
    public static final String POM_TYPE_TRAILER = "108";

    // Операции body
    public static final String OP_DR = "DR";
    public static final String OP_CR = "CR";
    public static final String OP_ZR = "ZR";
    public static final String REGEX_VALID_OPERATIONS = "DR|CR|ZR";

    // Описание операций
    public static final String DESC_ZR = "Обнуление счёта";
    public static final String DESC_CR = "Списание счёта";
    public static final String DESC_DR = "Зачисление на счёт";

    // из header процедуры
    public static final String DEFAULT_CURRENCY = "222";
    public static final String PROC_TYPE_IN_TIME = "IN-TIME";

    // Кодировка
    public static final Charset CHARSET_WINDOWS_1251 = Charset.forName("windows-1251");

    // Индексы парамтеров в строках ENROLL
    public static final int IDX_CREATED_START = 2;
    public static final int IDX_CREATED_END = 17;
    public static final int IDX_PROC_START = 18;
    public static final int IDX_PROC_END = 27;
    public static final int IDX_PROCESS_TIME_START = 27;
    public static final int IDX_PROCESS_TIME_END = 42;

    public static final int IDX_FIO_START = 0;
    public static final int IDX_FIO_END = 100;
    public static final int IDX_ACC_START = 100;
    public static final int IDX_ACC_END = 130;
    public static final int IDX_TYPE_START = 130;
    public static final int IDX_TYPE_END = 132;
    public static final int IDX_SUMM_START = 132;
    public static final int IDX_SUMM_END = 152;

    public static final int IDX_COUNT_START = 10;
    public static final int IDX_COUNT_END = 20;

    // Формат времени для ENROLL
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    // Длины и размеры строк
    public static final int LEN_HEADER = 42;
    public static final int LEN_BODY = 152;
    public static final int LEN_TRAILER = 20;

    public static final int LINE_SIZE_HEADER = LEN_HEADER + 2;
    public static final int LINE_SIZE_BODY = LEN_BODY + 2;
    public static final int LINE_SIZE_TRAILER = LEN_TRAILER + 2;

    // Регулярные выражения для валидации
    public static final Pattern REGEX_HEADER = Pattern.compile("^H \\d{8} \\d{6} .{24}$");
    public static final Pattern REGEX_TRAILER = Pattern.compile("^T\\s{9}\\s*\\d+$");
    public static final Pattern REGEX_BODY = Pattern.compile("^.{152}$");
    public static final Pattern REGEX_PROC_TYPE = Pattern.compile("^(IMMEDIATE|IN-TIME)$");
    public static final Pattern REGEX_NUMERIC = Pattern.compile("^\\d+$");
    public static final Pattern REGEX_FILENAME = Pattern.compile("^Z\\d{3}\\d{3}\\.[A-Z]+_ENROLL\\d{3}\\d{3}\\d\\.\\d{3}$");

    // Сообщение об ошибке для БД
    public static final int MAX_ERROR_MSG_LENGTH = 100;

    // Сообщение об ошибках и логи
    public static final String MSG_INVALID_FILENAME = "Invalid filename format";
    public static final String MSG_FILE_EMPTY = "File is empty";
    public static final String MSG_INVALID_HEADER = "Invalid header format";
    public static final String MSG_INVALID_TRAILER = "Invalid trailer format";
    public static final String MSG_FILE_TRAILER_HEADER_INVALID = "File trailer or header is invalid";
    public static final String MSG_ACCOUNT_EMPTY = "Account is empty";
    public static final String MSG_INVALID_OP_TYPE = "Invalid operation type: ";
    public static final String MSG_UPLOAD_SUCCESS = "File fully uploaded and processing started";
    public static final String MSG_MULTIPART_SUCCESS = "File processed successfully";

    public static final String MSG_FAILED_APPEND_CHUNK = "Failed to append gRPC chunk";
    public static final String MSG_FAILED_SAVE_CHUNK = "Failed to save chunked stream";
    public static final String MSG_FAILED_MOVE_FILE = "Cannot move file";
    public static final String MSG_FILE_SIZE_ERROR = "File exception: cant find size of file";

    // Коды
    public static final int HTTP_STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
}

