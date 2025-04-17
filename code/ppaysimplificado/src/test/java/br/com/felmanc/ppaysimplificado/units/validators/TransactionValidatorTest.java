package br.com.felmanc.ppaysimplificado.units.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.validators.TransactionValidator;

class TransactionValidatorTest {

    private UserEntity createUserEntity(Long id, BigDecimal balance, UserType type) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        user.setType(type);
        return user;
    }

    @Test
    void testValidarTransacao_Success() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        assertDoesNotThrow(() -> TransactionValidator.validarTransacao(payer, payee, value));
    }

    @Test
    void testValidarTransacao_SamePayerAndPayee() {
        UserEntity user = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(user, user, value));
        assertEquals("Pagador e recebedor não podem ser o mesmo.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_PayerIsMerchant() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.MERCHANT);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(payer, payee, value));
        assertEquals("Pagador não pode ser lojista.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsNull() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(payer, payee, null));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsZero() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = BigDecimal.ZERO;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(payer, payee, value));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_ValueIsNegative() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("-10.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(payer, payee, value));
        assertEquals("O valor da transação deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testValidarTransacao_InsufficientBalance() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("20.00"), UserType.COMMON);
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"), UserType.COMMON);
        BigDecimal value = new BigDecimal("30.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TransactionValidator.validarTransacao(payer, payee, value));
        assertEquals("Saldo insuficiente.", exception.getMessage());
    }
}