package br.com.felmanc.ppaysimplificado.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;

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
    
    public UserEntity createUser(UserEntity userEntity) {
        // Valida se o CPF já existe
        Optional<UserEntity> existingByCpf = userRepository.findByCpf(userEntity.getCpf());
        if (existingByCpf.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com este CPF.");
        }

        // Valida se o e-mail já existe
        Optional<UserEntity> existingByEmail = userRepository.findByEmail(userEntity.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }

        // Define saldo inicial e validações adicionais, se necessário
        if (userEntity.getBalance() == null) {
            userEntity.setBalance(0.0); // Saldo padrão inicial
        }

        if (userEntity.getType() == null) {
            throw new IllegalArgumentException("O tipo do usuário (COMMON ou MERCHANT) é obrigatório.");
        }

        // Salva o usuário no banco
        return userRepository.save(userEntity);
    }

    public List<UserEntity> getAllUsers() {
        // Busca todos os usuários no banco de dados
        return userRepository.findAll();
    }

    public UserEntity getUserById(Long id) {
        // Busca o usuário pelo ID e lança exceção caso não encontre
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário com o ID " + id + " não foi encontrado."));
    }
}
