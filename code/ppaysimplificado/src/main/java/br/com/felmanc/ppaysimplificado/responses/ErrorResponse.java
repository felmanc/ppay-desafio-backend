package br.com.felmanc.ppaysimplificado.responses;

public record ErrorResponse(
    String error,
    String message,
    String details
) {}
