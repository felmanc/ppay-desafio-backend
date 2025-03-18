package br.com.felmanc.ppaysimplificado.controllers;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
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

    @EventListener(ContextRefreshedEvent.class)
    public void checkBean() {
        log.info("TransactionService bean loaded: {}", (transactionService != null));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(@RequestBody TransactionDTO transactionDTO) {
        log.info("Recebida solicitação de transferência. Pagador: {}, Recebedor: {}, Valor: {}",
                transactionDTO.getIdPagador(), transactionDTO.getIdRecebedor(), transactionDTO.getValor());
        TransactionDTO response = transactionService.transfer(transactionDTO);
        log.info("Transferência realizada com sucesso. ID da Transação: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}
