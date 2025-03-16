package br.com.felmanc.ppaysimplificado.repositories;

import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
}
