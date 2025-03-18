package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;

import br.com.felmanc.ppaysimplificado.enums.UserType;

public record UserDTO(
    Long id,
    String nome,
    String cpf,
    String email,
    String senha,
    BigDecimal saldo,
    UserType tipo
) {}
