package org.example.exception;

public class DatabaseSaveException extends RuntimeException {
    public DatabaseSaveException(String entityName) {
        super("Failed to save entity to database: " + entityName);
    }
    public DatabaseSaveException(String entityName, Throwable cause) {
        super("Failed to save entity to database: " + entityName, cause);
    }
}
