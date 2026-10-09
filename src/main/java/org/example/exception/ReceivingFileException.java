package org.example.exception;

public class ReceivingFileException extends RuntimeException {
    public ReceivingFileException(String filename, String action) {
        super("Failed to " + action + " file: " + filename);
    }
    public ReceivingFileException(String filename, String action, Throwable cause) {
        super("Failed to " + action + " file: " + filename, cause);
    }
}
