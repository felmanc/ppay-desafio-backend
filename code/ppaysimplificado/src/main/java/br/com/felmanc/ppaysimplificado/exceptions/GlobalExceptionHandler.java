package br.com.felmanc.ppaysimplificado.exceptions;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(Map.of(
	                    "error", "BAD_REQUEST",
	                    "message", "Entrada inválida: " + ex.getLocalizedMessage(),
	                    "details", "A entrada fornecida não pôde ser analisada. Por favor, verifique o formato da entrada e tente novamente."
	            ));
	}

	@ExceptionHandler(UnauthorizedTransactionException.class)
	public ResponseEntity<Object> handleUnauthorizedTransactionException(UnauthorizedTransactionException ex) {
	    return ResponseEntity
	            .status(HttpStatus.FORBIDDEN)
	            .body(Map.of(
	                    "error", "FORBIDDEN",
	                    "message", ex.getMessage(),
	                    "details", "Transação não autorizada pelo serviço externo."
	            ));
	}   

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Object> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(Map.of(
	                    "error", "BAD_REQUEST",
	                    "message", String.format("O parâmetro '%s' é obrigatório e não foi fornecido.", ex.getParameterName()),
	                    "details", ex.getMessage()
	            ));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(Map.of(
	                    "error", "BAD_REQUEST",
	                    "message", "O parâmetro '" + ex.getName() + "' é inválido. Esperado: " + ex.getRequiredType().getSimpleName(),
	                    "details", ex.getMessage()
	            ));
	}

	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(Map.of(
	                    "error", "BAD_REQUEST",
	                    "message", ex.getMessage(),
	                    "details", "Uma requisição inválida foi enviada."
	            ));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralException(Exception ex) {
	    return ResponseEntity
	            .status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of(
	                    "error", "INTERNAL_SERVER_ERROR",
	                    "message", "Ocorreu um erro inesperado.",
	                    "details", ex.getMessage()
	            ));
	}
}
