package br.com.felmanc.ppaysimplificado.entities;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Double balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;
}
