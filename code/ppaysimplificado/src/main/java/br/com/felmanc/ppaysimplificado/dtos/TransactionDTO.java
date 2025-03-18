package br.com.felmanc.ppaysimplificado.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {
	private Long id;
	private Double valor;
    private Long idPagador;
    private Long idRecebedor;
    private String status;
    private LocalDateTime data;
}
