package br.com.felmanc.ppaysimplificado.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;
import br.com.felmanc.ppaysimplificado.services.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;
    
    public UserController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("Recebida requisição para criar usuário com CPF: {}", userDTO.getCpf());
        UserEntity userEntity = userMapper.toEntity(userDTO);
        UserEntity createdUser = userService.createUser(userEntity);
        UserDTO response = userMapper.toDTO(createdUser);
        log.info("Usuário criado com sucesso: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Recebida requisição para buscar todos os usuários");
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
        
        if (users.isEmpty()) {
            log.warn("Nenhum usuário encontrado");
            return ResponseEntity.noContent().build();
        }
        
        log.info("Número de usuários encontrados: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Recebida requisição para buscar o usuário com ID: {}", id);
        try {
            UserEntity userEntity = userService.getUserById(id);
            UserDTO userDTO = userMapper.toDTO(userEntity);
            log.info("Usuário encontrado: {}", userDTO.getId());
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao buscar usuário com ID: {}", id, e);
            throw e;
        }
    }
}
