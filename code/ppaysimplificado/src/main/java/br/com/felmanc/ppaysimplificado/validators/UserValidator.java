package br.com.felmanc.ppaysimplificado.validators;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;

@Component
public class UserValidator {

    public void validarSaldoSuficiente(UserEntity user, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do débito deve ser positivo.");
        }
        if (user.getBalance().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }

    public void validarDadosUsuario(String nome, String documento, String email, String senha, UserType tipo) {
        campoObrigatorio(nome, "O nome do usuário é obrigatório.");
        campoObrigatorio(documento, "O CPF/CNPJ é obrigatório.");
        campoObrigatorio(email, "O e-mail é obrigatório.");
        campoObrigatorio(senha, "A senha é obrigatória.");
        campoObrigatorio(tipo, "O tipo do usuário (COMMON ou MERCHANT) é obrigatório.");

        campoFormato(documento,
                "\\d{11}|\\d{14}",
                "O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.");
        campoFormato(email,
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                "O e-mail deve estar em um formato válido.");
    }

    private void campoObrigatorio(Object campo, String mensagem) {
        if (campo == null) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void campoFormato(String campo, String formato, String mensagem) {
        if (!campo.matches(formato)) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}