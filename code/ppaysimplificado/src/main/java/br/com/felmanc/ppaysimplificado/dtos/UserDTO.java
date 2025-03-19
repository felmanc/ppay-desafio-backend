package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserDTO(
	@Schema(hidden = true)
    Long id,
    String nome,
    String cpf,
    String email,
    String senha,
    BigDecimal saldo,
    UserType tipo
) {}
