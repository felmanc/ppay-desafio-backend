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
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LoggerUtil loggerUtil;

    public UserService(UserRepository userRepository, UserMapper userMapper, LoggerUtil loggerUtil) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.loggerUtil = loggerUtil;
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
        loggerUtil.logInfo("Validação", "Iniciando validação do usuário");

        try {
            campoObrigatorio(userDTO.nome(), "O nome do usuário é obrigatório.");
            campoObrigatorio(userDTO.cpf(), "O CPF/CNPJ é obrigatório.");
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
            loggerUtil.logDebug("Validação", "Usuário convertido para entidade");

            Optional<UserEntity> existingByCpf = userRepository.findByCpf(userEntity.getCpf());
            if (existingByCpf.isPresent()) {
                loggerUtil.logWarn("Validação", "Já existe um usuário com este CPF");
                throw new IllegalArgumentException("Já existe um usuário com este CPF.");
            }

            Optional<UserEntity> existingByEmail = userRepository.findByEmail(userEntity.getEmail());
            if (existingByEmail.isPresent()) {
                loggerUtil.logWarn("Validação", "Já existe um usuário com este e-mail");
                throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
            }

            loggerUtil.logInfo("Validação", "Validação concluída com sucesso");
            return userEntity;

        } catch (IllegalArgumentException e) {
            loggerUtil.logError("Validação", "Validação do usuário falhou: {}", e.getMessage());
            throw e;
        }
    }

    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) {
            loggerUtil.logError("Criação", "Tentativa de criação de usuário null");
            throw new IllegalArgumentException("Não é possível criar usuário null");
        }

        loggerUtil.logInfo("Criação", "Iniciando criação de usuário");

        try {
            UserEntity userEntity = validateUser(userDTO);
            loggerUtil.logInfo("Criação", "Validação do usuário concluída");
            
            userEntity.setBalance(Optional.ofNullable(userEntity.getBalance()).orElse(new BigDecimal("0.0")));
            loggerUtil.logDebug("Criação", "Saldo ajustado: {}", userEntity.getBalance());

            UserEntity savedUser = userRepository.save(userEntity);
            loggerUtil.logInfo("Criação", "Usuário criado com sucesso: ID {}", savedUser.getId());

            UserDTO createdUser = userMapper.toDTO(savedUser);
            loggerUtil.logDebug("Criação", "Usuário convertido para DTO");

            return createdUser;

        } catch (Exception e) {
            loggerUtil.logError("Criação", "Falha ao criar usuário: {}", e.getMessage());
            throw e;
        }
    }

    public List<UserDTO> getAllUsers() {
        loggerUtil.logInfo("Consulta", "Buscando todos os usuários");
        List<UserEntity> userEntities = userRepository.findAll();
        loggerUtil.logInfo("Consulta", "Número de usuários encontrados: {}", userEntities.size());
        return userEntities.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long id) {
        loggerUtil.logInfo("Consulta", "Buscando usuário pelo ID: {}", id);
        UserEntity userEntity = findUserEntityById(id);
        return userMapper.toDTO(userEntity);
    }

    public UserEntity findUserEntityById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> {
                    loggerUtil.logError("Consulta", "Usuário com ID {} não encontrado", id);
                    return new IllegalArgumentException("Usuário com o ID " + id + " não foi encontrado.");
                });
        return userEntity;
    }
}