package org.example.exception;

/**
 Выбрасывается при возникновении ошибок во время обработки содержимого файла таких как:
  несовпадение количества строк в Trailer, ошибки парсинга данных или
  некорректный формат значений (сумма, тип операции).
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String filename, String reason) {
        super("Processing failed for file '" + filename + "': " + reason);
    }
}
