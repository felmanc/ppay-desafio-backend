package br.com.felmanc.ppaysimplificado.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Iniciando criação de usuário com CPF: {}", userEntity.getCpf());

        Optional<UserEntity> existingByCpf = userRepository.findByCpf(userEntity.getCpf());
        if (existingByCpf.isPresent()) {
            log.warn("Tentativa de criar usuário com CPF duplicado: {}", userEntity.getCpf());
            throw new IllegalArgumentException("Já existe um usuário com este CPF.");
        }

        Optional<UserEntity> existingByEmail = userRepository.findByEmail(userEntity.getEmail());
        if (existingByEmail.isPresent()) {
            log.warn("Tentativa de criar usuário com e-mail duplicado: {}", userEntity.getEmail());
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }

        if (userEntity.getBalance() == null) {
            userEntity.setBalance(0.0); // Saldo padrão inicial
        }

        if (userEntity.getType() == null) {
            throw new IllegalArgumentException("O tipo do usuário (COMMON ou MERCHANT) é obrigatório.");
        }

        UserEntity savedUser = userRepository.save(userEntity);
        log.info("Usuário criado com sucesso: {}", savedUser.getId());

        return savedUser;
    }

    public List<UserEntity> getAllUsers() {
        log.info("Buscando todos os usuários");
        List<UserEntity> userEntities = userRepository.findAll();
        log.info("Obtidos todos os usuários. Encontrados: {}", userEntities.size());
        return userEntities;
    }

    public UserEntity getUserById(Long id) {
        log.info("Buscando usuário pelo ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário com ID {} não encontrado", id);
                    return new IllegalArgumentException("Usuário com o ID " + id + " não foi encontrado.");
                });
    }
}
