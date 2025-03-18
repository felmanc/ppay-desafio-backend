package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {
	private Long id;
	private BigDecimal valor;
    private Long idPagador;
    private Long idRecebedor;
    private String status;
    private LocalDateTime data;
}
