package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    Long id,
    Long idPagador,
    Long idRecebedor,
    BigDecimal valor,
    String status,
    LocalDateTime data
) {}
