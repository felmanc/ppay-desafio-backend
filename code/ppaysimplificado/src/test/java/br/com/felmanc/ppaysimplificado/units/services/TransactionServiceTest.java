package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.transaction.annotation.Transactional;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClient;
import br.com.felmanc.ppaysimplificado.clients.NotificationClientImpl;
import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
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
        assertEquals(new BigDecimal("100.00"), payer.getBalance());
        assertEquals(new BigDecimal("150.00"), payee.getBalance());
        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        verify(notificationClientImpl, times(1)).sendNotification(any(UserEntity.class), anyString());
    }
    
    @Test
    @Transactional
    void testCreateTransactionUnauthorized() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(1L, 1L, 2L, new BigDecimal("100.00"), TransactionStatus.PENDING.name(), null);

        // Configurando os dados iniciais
        UserEntity payer = new UserEntity();
        payer.setId(1L);
        payer.setBalance(new BigDecimal("200.00"));
        payer.setType(UserType.COMMON);
        //userService.save(payer);

        UserEntity payee = new UserEntity();
        payee.setId(2L);
        payee.setBalance(new BigDecimal("50.00"));
        payee.setType(UserType.COMMON);
        //userService.save(payee);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Simulando a falha de autorização
        //when(authorizationClient.authorizeTransaction()).thenThrow(new UnauthorizedTransactionException("Transação não autorizada pelo serviço externo."));
        when(authorizationClient.authorizeTransaction()).thenReturn(false);

        // Execução e Verificação
        Exception exception = assertThrows(UnauthorizedTransactionException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        assertEquals("Transação não autorizada pelo serviço externo.", exception.getMessage());

//        assertEquals(new BigDecimal("200.00"), payer.getBalance());
//        assertEquals(new BigDecimal("50.00"), payee.getBalance());
    }
    
    @Test
    void testCreateTransactionInsufficientBalance() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(1L, 1L, 2L, new BigDecimal("300.00"), TransactionStatus.PENDING.name(), null);
        
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

        // Execução e Verificação
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        assertEquals("Saldo insuficiente.", exception.getMessage());

        verify(transactionRepository, times(0)).save(any(TransactionEntity.class));
        verify(notificationClientImpl, times(0)).sendNotification(any(UserEntity.class), anyString());

//        assertEquals(new BigDecimal("200.00"), payer.getBalance());
//        assertEquals(new BigDecimal("50.00"), payee.getBalance());
    }
  
}