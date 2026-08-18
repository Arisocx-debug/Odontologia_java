package com.wilsonmontenegro.odontologia.exception;

/** Equivalente a abort(404) / findOrFail() de Laravel. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
