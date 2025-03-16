package br.com.felmanc.ppaysimplificado.services;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public void validateBalance(UserEntity payer, Double value) {
        if (payer.getBalance() < value) {
            throw new IllegalArgumentException("Saldo insuficiente para a transferência.");
        }
    }
}
