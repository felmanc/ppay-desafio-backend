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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("Recebida requisição para criar usuário com CPF: {}", userDTO.getCpf());
        UserEntity userEntity = UserMapper.INSTANCE.toEntity(userDTO);
        UserEntity createdUser = userService.createUser(userEntity);
        UserDTO response = UserMapper.INSTANCE.toDTO(createdUser);
        log.info("Usuário criado com sucesso: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Recebida requisição para buscar todos os usuários");
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
        log.info("Número de usuários encontrados: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Recebida requisição para buscar o usuário com ID: {}", id);
        try {
            UserEntity userEntity = userService.getUserById(id);
            UserDTO userDTO = UserMapper.INSTANCE.toDTO(userEntity);
            log.info("Usuário encontrado: {}", userDTO.getId());
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao buscar usuário com ID: {}", id, e);
            throw e;
        }
    }
}
