package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.services.UserBalanceService;
import br.com.felmanc.ppaysimplificado.validators.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserBalanceServiceTest {

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserBalanceService userBalanceService;

    private UserEntity createUserEntity(Long id, BigDecimal balance) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        user.setType(UserType.COMMON); // Tipo não é relevante para estes testes
        return user;
    }

    @Test
    void testDebitarSuccess() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"));
        BigDecimal debitValue = new BigDecimal("30.00");

        doNothing().when(userValidator).validarSaldoSuficiente(payer, debitValue);

        userBalanceService.debitar(payer, debitValue);
        assertEquals(new BigDecimal("70.00"), payer.getBalance());
    }

    @Test
    void testDebitarInsufficientBalance() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("50.00"));
        BigDecimal debitValue = new BigDecimal("100.00");

        doThrow(new IllegalArgumentException("Saldo insuficiente."))
                .when(userValidator).validarSaldoSuficiente(payer, debitValue);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.debitar(payer, debitValue);
        });
        assertEquals("Saldo insuficiente.", exception.getMessage());
        assertEquals(new BigDecimal("50.00"), payer.getBalance()); // Saldo não deve ser alterado
    }

    @Test
    void testDebitarInvalidValue() {
        UserEntity payer = createUserEntity(1L, new BigDecimal("100.00"));
        BigDecimal debitValue = new BigDecimal("-10.00");

        doThrow(new IllegalArgumentException("O valor do débito deve ser positivo."))
                .when(userValidator).validarSaldoSuficiente(payer, debitValue);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.debitar(payer, debitValue);
        });
        assertEquals("O valor do débito deve ser positivo.", exception.getMessage());
        assertEquals(new BigDecimal("100.00"), payer.getBalance()); // Saldo não deve ser alterado
    }

    @Test
    void testCreditarSuccess() {
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"));
        BigDecimal creditValue = new BigDecimal("100.00");

        userBalanceService.creditar(payee, creditValue);
        assertEquals(new BigDecimal("150.00"), payee.getBalance());
    }

    @Test
    void testCreditarInvalidValueZero() {
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"));
        BigDecimal creditValue = BigDecimal.ZERO;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.creditar(payee, creditValue);
        });
        assertEquals("O valor do crédito deve ser positivo.", exception.getMessage());
        assertEquals(new BigDecimal("50.00"), payee.getBalance()); // Saldo não deve ser alterado
    }

    @Test
    void testCreditarInvalidValueNegative() {
        UserEntity payee = createUserEntity(2L, new BigDecimal("50.00"));
        BigDecimal creditValue = new BigDecimal("-10.00");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.creditar(payee, creditValue);
        });
        assertEquals("O valor do crédito deve ser positivo.", exception.getMessage());
        assertEquals(new BigDecimal("50.00"), payee.getBalance()); // Saldo não deve ser alterado
    }
}