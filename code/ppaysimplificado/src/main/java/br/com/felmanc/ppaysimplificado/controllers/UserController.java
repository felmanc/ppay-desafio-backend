package br.com.felmanc.ppaysimplificado.controllers;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;
import br.com.felmanc.ppaysimplificado.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserEntity userEntity = UserMapper.INSTANCE.toEntity(userDTO);
        UserEntity createdUser = userService.createUser(userEntity);
        UserDTO response = UserMapper.INSTANCE.toDTO(createdUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserEntity userEntity = userService.getUserById(id);
        UserDTO userDTO = UserMapper.INSTANCE.toDTO(userEntity);
        return ResponseEntity.ok(userDTO);
    }
}
