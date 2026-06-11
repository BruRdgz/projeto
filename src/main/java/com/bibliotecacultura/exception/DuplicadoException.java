package com.bibliotecacultura.exception;

/** Thrown when a unique field (CPF, matricula) already exists in the DB. */
public class DuplicadoException extends NegocioException {
    public DuplicadoException(String message) {
        super(message);
    }
}
