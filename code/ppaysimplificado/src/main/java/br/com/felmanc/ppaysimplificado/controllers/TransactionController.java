package br.com.felmanc.ppaysimplificado.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.mappers.TransactionMapper;
import br.com.felmanc.ppaysimplificado.services.TransactionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(@RequestBody TransactionDTO transactionDTO) {
        log.info("Recebida solicitação de transferência. Pagador: {}, Recebedor: {}, Valor: {}",
                transactionDTO.getPayerId(), transactionDTO.getPayeeId(), transactionDTO.getValue());
        TransactionEntity transactionEntity = transactionService.transfer(
                transactionDTO.getPayerId(),
                transactionDTO.getPayeeId(),
                transactionDTO.getValue()
        );
        TransactionDTO response = TransactionMapper.INSTANCE.toDTO(transactionEntity);
        log.info("Transferência realizada com sucesso. ID da Transação: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}
