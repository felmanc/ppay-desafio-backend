package br.com.felmanc.ppaysimplificado.entities;

import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double value;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private UserEntity payer;

    @ManyToOne
    @JoinColumn(name = "payee_id", nullable = false)
    private UserEntity payee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
