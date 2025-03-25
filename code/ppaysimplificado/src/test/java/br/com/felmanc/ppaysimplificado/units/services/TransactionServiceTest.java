package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
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
    
    private UserEntity createUserEntity(Long id, BigDecimal balance, UserType type) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        user.setType(type);
        return user;
    }

    private TransactionEntity createTransaction(Long id, UserEntity payer, UserEntity payee, BigDecimal value, TransactionStatus status) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(id);
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setValue(value);
        transaction.setStatus(status);
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }
    
    @Test
    void testGetAllTransactions() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("200.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        TransactionEntity transaction = createTransaction(1L, payer, payee, new BigDecimal("100.00"), TransactionStatus.PENDING);

        List<TransactionEntity> transactions = Arrays.asList(transaction);
        when(transactionRepository.findAll()).thenReturn(transactions);
        when(transactionMapper.toDTOList(transactions)).thenAnswer(invocation -> {
            List<TransactionEntity> trans = invocation.getArgument(0);
            return trans.stream()
                .map(tr -> new TransactionDTO(tr.getId(), tr.getPayer().getId(), tr.getPayee().getId(), tr.getValue(), tr.getStatus().name(), tr.getTimestamp()))
                .toList();
        });

        List<TransactionDTO> result = transactionService.getAllTransactions();
        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).id());
    }

    @Test
    void testGetAllTransactionsEmpty() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        List<TransactionDTO> result = transactionService.getAllTransactions();
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllTransactionsMultiple() {
        UserEntity payer1 = createUserEntity(1L, new BigDecimal("200.00"), UserType.COMMON);
        UserEntity payee1 = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        TransactionEntity transaction1 = createTransaction(1L, payer1, payee1, new BigDecimal("100.00"), TransactionStatus.PENDING);

        UserEntity payer2 = createUserEntity(3L, new BigDecimal("300.00"), UserType.COMMON);
        UserEntity payee2 = createUserEntity(4L, new BigDecimal("150.00"), UserType.COMMON);
        TransactionEntity transaction2 = createTransaction(2L, payer2, payee2, new BigDecimal("200.00"), TransactionStatus.PENDING);

        List<TransactionEntity> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);
        when(transactionMapper.toDTOList(transactions)).thenAnswer(invocation -> {
            List<TransactionEntity> trans = invocation.getArgument(0);
            return trans.stream()
                .map(tr -> new TransactionDTO(tr.getId(), tr.getPayer().getId(), tr.getPayee().getId(), tr.getValue(), tr.getStatus().name(), tr.getTimestamp()))
                .toList();
        });

        List<TransactionDTO> result = transactionService.getAllTransactions();
        assertEquals(2, result.size());
        assertEquals(transaction1.getId(), result.get(0).id());
        assertEquals(transaction2.getId(), result.get(1).id());
    }    
    
    @Test
    void testCreateTransactionSuccess() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(1L, 1L, 2L, new BigDecimal("100.00"), TransactionStatus.COMPLETED.name(), null);

        // Mocking
        UserEntity payer = createUserEntity(1L, new BigDecimal("200.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authorizationClient.authorizeTransaction()).thenReturn(true);
        when(notificationClientImpl.sendNotification(any(UserEntity.class), anyString())).thenReturn(true);
        when(transactionMapper.toDTO(any(TransactionEntity.class))).thenReturn(transactionDTO);

        // Execução
        TransactionDTO result = assertDoesNotThrow(() -> transactionService.createTransaction(transactionDTO));

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

        // Mocking
        UserEntity payer = createUserEntity(1L, new BigDecimal("200.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
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
        UserEntity payer = createUserEntity(1L, new BigDecimal("200.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);

        // Execução e Verificação
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        assertEquals("Saldo insuficiente.", exception.getMessage());
        verify(transactionRepository, times(0)).save(any(TransactionEntity.class));
        verify(notificationClientImpl, times(0)).sendNotification(any(UserEntity.class), anyString());

        assertEquals(new BigDecimal("200.00"), payer.getBalance());
        assertEquals(new BigDecimal("50.00"), payee.getBalance());
    }

    @Test
    void testCreateTransactionInvalidValue() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(null, 1L, 2L, BigDecimal.ZERO, null, LocalDateTime.now());
        UserEntity payer = new UserEntity(1L, "Payer", "12345678900", "payer@example.com", "password", BigDecimal.valueOf(200), UserType.COMMON);
        UserEntity payee = new UserEntity(2L, "Payee", "09876543211", "payee@example.com", "password", BigDecimal.valueOf(50), UserType.COMMON);

        // Mocking
        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);

        // Execução e Verificação
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        String expectedMessage = "O valor da transação deve ser maior que zero.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testCreateTransactionSamePayerAndPayee() {
        // Dados de entrada
        TransactionDTO transactionDTO = new TransactionDTO(null, 1L, 1L, BigDecimal.TEN, null, LocalDateTime.now());
        UserEntity payer = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.valueOf(200), UserType.COMMON);

        // Mocking
        when(userService.findUserEntityById(1L)).thenReturn(payer);

        // Execução e Verificação
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        String expectedMessage = "Pagador e recebedor não podem ser o mesmo.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}