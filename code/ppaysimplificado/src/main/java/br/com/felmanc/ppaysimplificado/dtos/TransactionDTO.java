package br.com.felmanc.ppaysimplificado.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {
    private Long id;
    private Double value;
    private Long payerId; // ID do pagador
    private Long payeeId; // ID do recebedor
    private String status;
    private LocalDateTime timestamp;
}
