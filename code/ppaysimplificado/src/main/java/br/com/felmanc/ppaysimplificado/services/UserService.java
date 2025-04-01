package br.com.felmanc.ppaysimplificado.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    private void campoObrigatorio(Object campo, String mensagem) {
        if (campo == null) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void campoFormato(String campo, String formato, String mensagem) {
        if (!campo.matches(formato)) {
            throw new IllegalArgumentException(mensagem);
        }
    }    
    
    private UserEntity validateUser(UserDTO userDTO) {
        log.info("[Validação] Iniciando validação do usuário com CPF: {}", userDTO.cpf());

        try {
            campoObrigatorio(userDTO.nome(), "O nome do usuário é obrigatório.");
            campoObrigatorio(userDTO.cpf(), "O CPF/ CNPJ é obrigatório.");
            campoObrigatorio(userDTO.email(), "O e-mail é obrigatório.");
            campoObrigatorio(userDTO.senha(), "A senha é obrigatória.");
            campoObrigatorio(userDTO.tipo(), "O tipo do usuário (COMMON ou MERCHANT) é obrigatório.");
            
            campoFormato(userDTO.cpf(),
                    "\\d{11}|\\d{14}",
                    "O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.");
            campoFormato(userDTO.email(),
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                    "O e-mail deve estar em um formato válido.");
            
            UserEntity userEntity = userMapper.toEntity(userDTO);
            log.debug("[Validação] Usuário convertido para entidade: {}", userEntity);

            Optional<UserEntity> existingByCpf = userRepository.findByCpf(userEntity.getCpf());
            if (existingByCpf.isPresent()) {
                log.warn("[Validação] Já existe um usuário com este CPF: {}", userEntity.getCpf());
                throw new IllegalArgumentException("Já existe um usuário com este CPF.");
            }

            Optional<UserEntity> existingByEmail = userRepository.findByEmail(userEntity.getEmail());
            if (existingByEmail.isPresent()) {
                log.warn("[Validação] Já existe um usuário com este e-mail: {}", userEntity.getEmail());
                throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
            }

            log.info("[Validação] Validação concluída com sucesso: {}", userEntity);
            return userEntity;

        } catch (IllegalArgumentException e) {
            log.error("[Erro] Validação do usuário falhou: {}", e.getMessage());
            throw e;
        }
    }

    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) {
            log.error("[Erro] Tentativa de criação de usuário null");
            throw new IllegalArgumentException("Não é possível criar usuário null");
        }

        log.info("[Criação] Iniciando criação de usuário com CPF: {}", userDTO.cpf());

        try {
            UserEntity userEntity = validateUser(userDTO);
            log.info("[Criação] Validação do usuário concluída: {}", userEntity);
            
            userEntity.setBalance(Optional.ofNullable(userEntity.getBalance()).orElse(new BigDecimal("0.0")));
            log.debug("[Criação] Saldo ajustado: {}", userEntity.getBalance());

            UserEntity savedUser = userRepository.save(userEntity);
            log.info("[Criação] Usuário criado com sucesso: ID {}", savedUser.getId());

            UserDTO createdUser = userMapper.toDTO(savedUser);
            log.debug("[Criação] Usuário convertido para DTO: {}", createdUser);

            if (createdUser == null) {
                log.error("[Erro] Conversão falhou: UserMapper retornou null");
            }

            return createdUser;

        } catch (Exception e) {
            log.error("[Erro] Falha ao criar usuário: {}", e.getMessage());
            throw e;
        }
    }


    public List<UserDTO> getAllUsers() {
        log.info("Buscando todos os usuários");
        List<UserEntity> userEntities = userRepository.findAll();
        log.info("Número de usuários encontrados: {}", userEntities.size());
        return userEntities.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long id) {
        log.info("Buscando usuário pelo ID: {}", id);
        UserEntity userEntity = findUserEntityById(id);
        return userMapper.toDTO(userEntity);
    }

    public UserEntity findUserEntityById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário com ID {} não encontrado", id);
                    return new IllegalArgumentException("Usuário com o ID " + id + " não foi encontrado.");
                });
        return userEntity;
    }
}
