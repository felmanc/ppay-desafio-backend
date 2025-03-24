package br.com.felmanc.ppaysimplificado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClientImpl;
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

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationClientImpl notificationClientImpl;    
    
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationClientImpl notificationService;

    @Mock
    private WebClient webClient;

    @Mock
    private TransactionMapper transactionMapper;
    
    @Spy
    private AuthorizationClientImpl authorizationClientImpl;

    @Test
    void shouldAuthorizeTransactionSuccessfully() {
        // Criar um spy do service para mockar o método privado
        TransactionService spyService = Mockito.spy(transactionService);

        // Mock do comportamento do método privado authorizeTransaction
        doReturn(true).when(spyService).authorizeTransaction(any(TransactionEntity.class));

        // Criar DTO de teste com TransactionStatus PENDING
        TransactionDTO transactionDTO = new TransactionDTO(
            null,  // ID será gerado na persistência
            1L,  // idPagador
            2L,  // idRecebedor
            new BigDecimal("100"), // valor
            null, // status
            null
        );

        // Mockando o retorno dos usuários e do repositório
        UserEntity payer = new UserEntity(null, "User", "CPF", "email", "senha", new BigDecimal("500"), UserType.COMMON);
        UserEntity payee = new UserEntity(null, "Merchant", "CNPJ", "email", "senha", new BigDecimal("1000"), UserType.MERCHANT);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity transaction = invocation.getArgument(0);
            transaction.setId(10L); // Simula o ID gerado pelo banco
            return transaction;
        });

        // Executar a transferência
        TransactionDTO result = spyService.createTransaction(transactionDTO);

        // Verificações
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED.name(), result.status()); // Transação finalizada com sucesso
        assertEquals(new BigDecimal("400"), payer.getBalance()); // Saldo atualizado
        assertEquals(new BigDecimal("1100"), payee.getBalance()); // Saldo atualizado

        // Verifica se o método foi chamado
        verify(spyService).authorizeTransaction(any(TransactionEntity.class));
    }
    
    @Test
    void testAuthorizeTransactionCalled() {
        TransactionDTO transactionDTO = new TransactionDTO(null, 1L, 2L, BigDecimal.TEN, null, null);
        UserEntity payer = new UserEntity(1L, "Payer", "12345678900", "payer@example.com", "password", BigDecimal.valueOf(100), UserType.COMMON);
        UserEntity payee = new UserEntity(2L, "Payee", "09876543211", "payee@example.com", "password", BigDecimal.valueOf(50), UserType.COMMON);

        when(userService.findUserEntityById(1L)).thenReturn(payer);
        when(userService.findUserEntityById(2L)).thenReturn(payee);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(true).when(authorizationClientImpl).authorizeTransaction();
        when(notificationClientImpl.sendNotification(any(), anyString())).thenReturn(true);
        when(transactionMapper.toDTO(any(TransactionEntity.class))).thenReturn(transactionDTO);

        transactionService.createTransaction(transactionDTO);

        verify(authorizationClientImpl).authorizeTransaction();
    }
}
