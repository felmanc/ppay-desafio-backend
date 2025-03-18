package br.com.felmanc.ppaysimplificado.dtos;

import java.math.BigDecimal;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	private String nome;
    private String cpf;
    private String email;
    private String senha;
    private BigDecimal saldo;
    private UserType tipo;
}
