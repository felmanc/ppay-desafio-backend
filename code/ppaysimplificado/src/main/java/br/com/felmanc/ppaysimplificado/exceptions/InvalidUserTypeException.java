package br.com.felmanc.ppaysimplificado.exceptions;

public class InvalidUserTypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidUserTypeException(String message) {
        super(message);
    }
}
