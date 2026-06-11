package com.bibliotecacultura.exception;

/** Thrown when a looked-up entity simply doesn't exist. */
public class EntidadeNaoEncontradaException extends NegocioException {
    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
