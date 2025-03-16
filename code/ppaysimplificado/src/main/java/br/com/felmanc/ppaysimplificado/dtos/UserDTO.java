package br.com.felmanc.ppaysimplificado.dtos;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String cpf;
    private String email;
    private Double balance;
    private UserType type;
}
