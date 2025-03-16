package br.com.felmanc.ppaysimplificado.repositories;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByCpf(String cpf);
    Optional<UserEntity> findByEmail(String email);
}
