package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransactionDTO(
	@Schema(hidden = true)
	Long id,
    Long idPagador,
    Long idRecebedor,
    BigDecimal valor,
	@Schema(hidden = true)
    String status,
	@Schema(hidden = true)
    LocalDateTime data
) {}
