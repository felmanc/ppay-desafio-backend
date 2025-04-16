package br.com.felmanc.ppaysimplificado.validations;

import java.math.BigDecimal;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;

public class UserValidator {

    public static void validarSaldo(UserEntity user, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do débito deve ser positivo.");
        }
        if (user.getBalance().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }
}
