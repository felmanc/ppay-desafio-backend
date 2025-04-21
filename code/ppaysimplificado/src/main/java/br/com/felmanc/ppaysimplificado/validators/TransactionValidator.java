package br.com.felmanc.ppaysimplificado.validators;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

@Component
public class TransactionValidator {

    private final LoggerUtil loggerUtil;

    public TransactionValidator(LoggerUtil loggerUtil) {
        this.loggerUtil = loggerUtil;
    }

    public void validateTransaction(TransactionDTO transactionDTO, UserEntity payer, UserEntity payee) {
        if (transactionDTO == null) {
            loggerUtil.logError("Validação", "O objeto TransactionDTO é nulo.");
            throw new IllegalArgumentException("O objeto TransactionDTO não pode ser nulo.");
        }

        if (payer == null) {
            loggerUtil.logError("Validação", "Pagador não encontrado. ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Pagador não encontrado.");
        }

        if (payee == null) {
            loggerUtil.logError("Validação", "Recebedor não encontrado. ID: {}", transactionDTO.idRecebedor());
            throw new IllegalArgumentException("Recebedor não encontrado.");
        }

        if (payer.equals(payee)) {
            loggerUtil.logError("Validação", "Pagador e recebedor não podem ser o mesmo.");
            throw new IllegalArgumentException("Pagador e recebedor não podem ser o mesmo.");
        }

        if (payer.getType() == UserType.MERCHANT) {
            loggerUtil.logError("Validação", "Pagador não pode ser lojista.");
            throw new IllegalArgumentException("Pagador não pode ser lojista.");
        }

        if (transactionDTO.valor() == null || transactionDTO.valor().compareTo(BigDecimal.ZERO) <= 0) {
            loggerUtil.logError("Validação", "O valor da transação deve ser maior que zero.");
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        if (payer.getBalance().compareTo(transactionDTO.valor()) < 0) {
            loggerUtil.logError("Validação", "Saldo insuficiente para transferência. Saldo: {}, Valor: {}",
                    payer.getBalance(), transactionDTO.valor());
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }
    }
}