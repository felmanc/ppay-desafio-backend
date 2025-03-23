package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record TransactionDTO(
	@Schema(hidden = true)
	Long id,
    Long idPagador,
    Long idRecebedor,
    BigDecimal valor,
	@Schema(hidden = true)
	TransactionStatus status,
	@Schema(hidden = true)
    LocalDateTime data
) {}
