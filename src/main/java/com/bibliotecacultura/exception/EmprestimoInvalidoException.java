package com.bibliotecacultura.exception;

/**
 * Thrown when a loan, renewal, or return operation violates a business rule:
 * client is blocked, already has 3 active loans, book is unavailable, etc.
 */
public class EmprestimoInvalidoException extends NegocioException {
    public EmprestimoInvalidoException(String message) {
        super(message);
    }
}
