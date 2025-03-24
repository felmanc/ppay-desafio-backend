package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClient;
import br.com.felmanc.ppaysimplificado.clients.NotificationClientImpl;
import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.mappers.TransactionMapper;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import br.com.felmanc.ppaysimplificado.services.TransactionService;
import br.com.felmanc.ppaysimplificado.services.UserService;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private NotificationClientImpl notificationClientImpl;
    
    @Mock
    private TransactionMapper transactionMapper;
    
    @Mock
    private AuthorizationClient authorizationClient;
    
    @InjectMocks
    private TransactionService transactionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTransaction() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(1L, 1L, 2L, new BigDecimal("100.00"), TransactionStatus.COMPLETED.name(), null);
        
        // Mocking
        UserEntity payer = new UserEntity();
        payer.setId(1L);
        payer.setBalance(new BigDecimal("200.00"));
        payer.setType(UserType.COMMON);
        
        UserEntity payee = new UserEntity();
        payee.setId(2L);
        payee.setBalance(new BigDecimal("50.00"));
        payee.setType(UserType.COMMON);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authorizationClient.authorizeTransaction()).thenReturn(true);
        when(notificationClientImpl.sendNotification(any(UserEntity.class), anyString())).thenReturn(true);
        when(transactionMapper.toDTO(any(TransactionEntity.class))).thenReturn(transactionDTO);

        // Execução
        TransactionDTO result = transactionService.createTransaction(transactionDTO);

        // Verificações
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED.name(), result.status());
        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        verify(notificationClientImpl, times(1)).sendNotification(any(UserEntity.class), anyString());
    }
}