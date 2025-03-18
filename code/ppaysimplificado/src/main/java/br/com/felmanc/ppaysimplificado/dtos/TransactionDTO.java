package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    Long id,
    BigDecimal valor,
    Long idPagador,
    Long idRecebedor,
    String status,
    LocalDateTime data
) {}
