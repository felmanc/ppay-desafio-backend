package br.com.felmanc.ppaysimplificado.controllers;

import java.util.List;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.services.TransactionService;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Transaction Controller", description = "APIs relacionadas a operações de transação")
@RestController
@RequestMapping("/transfer")
@Tag(name = "Transaction Controller", description = "APIs relacionadas a operações de transação")
public class TransactionController {

    private final TransactionService transactionService;
    private final LoggerUtil loggerUtil;

    public TransactionController(TransactionService transactionService, LoggerUtil loggerUtil) {
        this.transactionService = transactionService;
        this.loggerUtil = loggerUtil;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void checkBean() {
        loggerUtil.logInfo("Bean", "TransactionService bean loaded: {}.", (transactionService != null));
    }

    @Operation(summary = "Realiza uma transferência")
    @PostMapping
    public ResponseEntity<TransactionDTO> transfer(@Parameter(description = "Dados da transação", required = true) @Valid @RequestBody TransactionDTO transactionDTO) {
        loggerUtil.logInfo("Transferência", "Recebida solicitação de transferência. Pagador: {}, Recebedor: {}, Valor: {}.",
                transactionDTO.idPagador(), transactionDTO.idRecebedor(), transactionDTO.valor());
        TransactionDTO response = transactionService.createTransaction(transactionDTO);
        loggerUtil.logInfo("Transferência", "Transferência realizada com sucesso. ID da Transação: {}.", response.id());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retorna todas as transações")
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        loggerUtil.logInfo("Consulta", "Recebida solicitação para listar todas as transações.");
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        loggerUtil.logInfo("Consulta", "Total de transações retornadas: {}.", transactions.size());
        return ResponseEntity.ok(transactions);
    }
}