package br.com.felmanc.ppaysimplificado.validations;

import java.math.BigDecimal;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;

public class TransactionValidator {

    public static void validarTransacao(UserEntity payer, UserEntity payee, BigDecimal value) {

        if (payer.equals(payee)) {
            throw new IllegalArgumentException("Pagador e recebedor não podem ser o mesmo.");
        }
        if (payer.getType() == UserType.MERCHANT) {
            throw new IllegalArgumentException("Pagador não pode ser lojista.");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }
        if (payer.getBalance().compareTo(value) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }
}

