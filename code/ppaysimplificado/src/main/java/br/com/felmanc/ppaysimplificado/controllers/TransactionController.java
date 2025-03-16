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

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(@RequestBody TransactionDTO transactionDTO) {

        TransactionEntity createdTransaction = transactionService.transfer(
                transactionDTO.getPayerId(),
                transactionDTO.getPayeeId(),
                transactionDTO.getValue()
        );
        TransactionDTO response = TransactionMapper.INSTANCE.toDTO(createdTransaction);
        return ResponseEntity.ok(response);
    }
}
