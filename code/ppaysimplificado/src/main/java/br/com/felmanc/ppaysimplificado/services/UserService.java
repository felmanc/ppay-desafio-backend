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

        Optional<UserEntity> existingByCpf = userRepository.findByCpf(userEntity.getCpf());
        if (existingByCpf.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com este CPF.");
        }

        Optional<UserEntity> existingByEmail = userRepository.findByEmail(userEntity.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }

        return userEntity;
    }
    
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Iniciando criação de usuário com CPF: {}", userDTO.cpf());
        UserEntity userEntity = validateUser(userDTO);
        
        userEntity.setBalance(Optional.ofNullable(userEntity.getBalance()).orElse(new BigDecimal("0.0")));
        
        UserEntity savedUser = userRepository.save(userEntity);
        log.info("Usuário criado com sucesso: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
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
