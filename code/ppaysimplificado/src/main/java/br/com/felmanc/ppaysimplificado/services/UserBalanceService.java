package br.com.felmanc.ppaysimplificado.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.validations.UserValidator;

@Service
public class UserBalanceService {

    public static void debitar(UserEntity payer, BigDecimal valor) {
    	UserValidator.validarSaldo(payer, valor);
        payer.setBalance(payer.getBalance().subtract(valor));
    }

    public static void creditar(UserEntity payee, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do crédito deve ser positivo.");
        }
        payee.setBalance(payee.getBalance().add(valor));
    }
}
