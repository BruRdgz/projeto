package com.bibliotecacultura.exception;

/**
 * Base for all domain rule violations.
 * Controllers catch this and render a user-facing error message.
 */
public class NegocioException extends RuntimeException {
    public NegocioException(String message) {
        super(message);
    }
}
