package br.com.felmanc.ppaysimplificado.units.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import br.com.felmanc.ppaysimplificado.controllers.TransactionController;
import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.services.TransactionService;

public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTransfer() {
        TransactionDTO transactionDTO = new TransactionDTO(null, 1L, 2L, BigDecimal.valueOf(100.0), null, null);
        TransactionDTO createdTransaction = new TransactionDTO(1L, 1L, 2L, BigDecimal.valueOf(100.0), "SUCCESS", LocalDateTime.now());
        when(transactionService.createTransaction(transactionDTO)).thenReturn(createdTransaction);

        ResponseEntity<TransactionDTO> response = transactionController.transfer(transactionDTO);

        assertEquals(ResponseEntity.ok(createdTransaction), response);
    }

    @Test
    public void testTransferError() {
        TransactionDTO transactionDTO = new TransactionDTO(null, 1L, 2L, BigDecimal.valueOf(100.0), null, null);
        when(transactionService.createTransaction(transactionDTO)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao criar transação"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            transactionController.transfer(transactionDTO);
        });

        assertEquals("400 BAD_REQUEST \"Erro ao criar transação\"", exception.getMessage());
    }

    @Test
    public void testGetAllTransactions() {
        List<TransactionDTO> transactions = Arrays.asList(
                new TransactionDTO(1L, 1L, 2L, BigDecimal.valueOf(100.0), "SUCCESS", LocalDateTime.now()),
                new TransactionDTO(2L, 3L, 4L, BigDecimal.valueOf(200.0), "SUCCESS", LocalDateTime.now())
        );
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();

        assertEquals(ResponseEntity.ok(transactions), response);
    }

    @Test
    public void testGetAllTransactionsError() {
        when(transactionService.getAllTransactions()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar transações"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            transactionController.getAllTransactions();
        });

        assertEquals("500 INTERNAL_SERVER_ERROR \"Erro ao buscar transações\"", exception.getMessage());
    }
}