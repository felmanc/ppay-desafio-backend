package br.com.felmanc.ppaysimplificado.units.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import br.com.felmanc.ppaysimplificado.validators.TransactionValidator;

class TransactionValidatorTest {

    private TransactionValidator transactionValidator;
    private LoggerUtil loggerUtil;

    private UserEntity createUserEntity(Long id, BigDecimal balance, UserType type) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        user.setType(type);
        return user;
    }

    @BeforeEach
    void setUp() {
        loggerUtil = mock(LoggerUtil.class); // Criamos um "mock" de LoggerUtil
        transactionValidator = new TransactionValidator(loggerUtil); // Injectamos o mock no validator
    }

    @Test
    void testValidarTransacao_Success() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), value, null, null);
        assertDoesNotThrow(() -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
    }

    @Test
    void testValidarTransacao_SamePayerAndPayee() {
        UserEntity user = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        TransactionDTO transactionDTO = new TransactionDTO(null, user.getId(), user.getId(), value, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, user, user));
        assertEquals("Pagador e recebedor não podem ser o mesmo.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_PayerIsMerchant() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.MERCHANT);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), value, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
        assertEquals("Pagador não pode ser lojista.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsNull() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), null, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsZero() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = BigDecimal.ZERO;
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), value, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsNegative() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("-10.00");
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), value, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_InsufficientBalance() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("20.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        TransactionDTO transactionDTO = new TransactionDTO(null, payer.getId(), payee.getId(), value, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionValidator.validateTransaction(transactionDTO, payer, payee));
        assertEquals("Saldo insuficiente para transferência.", exception.getMessage());
    }
}