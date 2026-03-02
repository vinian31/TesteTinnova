package org.tinnova.teste.exception;

public class DolarAPIException extends RuntimeException {
    public DolarAPIException(String message) {
        super(message);
    }

    public DolarAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

