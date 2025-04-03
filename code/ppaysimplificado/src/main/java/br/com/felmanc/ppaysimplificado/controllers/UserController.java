package br.com.felmanc.ppaysimplificado.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.services.UserService;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User Controller", description = "APIs relacionadas a operações de usuário")
@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "APIs relacionadas a operações de usuário")
public class UserController {

    private final UserService userService;
    private final LoggerUtil loggerUtil;

    public UserController(UserService userService, LoggerUtil loggerUtil) {
        this.userService = userService;
        this.loggerUtil = loggerUtil;
    }

    @Operation(summary = "Cria um novo usuário")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "Dados do novo usuário", required = true)
            @Valid @RequestBody UserDTO userDTO) {

        loggerUtil.logInfo("Usuário", "Recebida requisição para criar usuário: {}", userDTO);

        UserDTO response = userService.createUser(userDTO);
        loggerUtil.logInfo("Usuário", "Usuário criado com sucesso: {}", response != null ? response.nome() : "null");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retorna todos os usuários")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        loggerUtil.logInfo("Usuário", "Recebida requisição para buscar todos os usuários");
        List<UserDTO> users = userService.getAllUsers();
        if (users.isEmpty()) {
            loggerUtil.logWarn("Usuário", "Nenhum usuário encontrado");
            return ResponseEntity.noContent().build();
        }
        loggerUtil.logInfo("Usuário", "Número de usuários encontrados: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Retorna um usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id) {
        loggerUtil.logInfo("Usuário", "Recebida requisição para buscar o usuário com ID: {}", id);
        UserDTO userDTO = userService.getUserById(id);
        loggerUtil.logInfo("Usuário", "Usuário encontrado: {}", userDTO.nome());
        return ResponseEntity.ok(userDTO);
    }
}