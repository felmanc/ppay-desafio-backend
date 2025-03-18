package br.com.felmanc.ppaysimplificado.dtos;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	private String nome;
    private String cpf;
    private String email;
    private String senha;
    private Double saldo;
    private UserType tipo;
}
