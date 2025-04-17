package br.com.felmanc.ppaysimplificado.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.validators.UserValidator;

@Service
public class UserBalanceService {

	private final UserValidator userValidator;
	
    public UserBalanceService(UserValidator userValidator) {
		this.userValidator = userValidator;
	}

	public void debitar(UserEntity payer, BigDecimal valor) {
    	userValidator.validarSaldoSuficiente(payer, valor);
        payer.setBalance(payer.getBalance().subtract(valor));
    }

    public void creditar(UserEntity payee, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do crédito deve ser positivo.");
        }
        payee.setBalance(payee.getBalance().add(valor));
    }
}
