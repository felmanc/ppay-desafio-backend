package br.com.felmanc.ppaysimplificado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedTransactionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedTransactionException(String message) {
        super(message);
    }
}