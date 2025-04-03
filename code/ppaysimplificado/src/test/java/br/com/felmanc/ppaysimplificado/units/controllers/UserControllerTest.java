package br.com.felmanc.ppaysimplificado.units.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import br.com.felmanc.ppaysimplificado.controllers.UserController;
import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.services.UserService;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

public class UserControllerTest {

    @Mock
    private UserService userService;
	
    @Mock
    private LoggerUtil loggerUtil;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        UserDTO createdUser = new UserDTO(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userService.createUser(userDTO)).thenReturn(createdUser);

        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().nome());
        assertEquals("12345678900", response.getBody().cpf());
    }

    @Test
    public void testCreateUserError() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "123.456.789-00", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userService.createUser(userDTO)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao criar usuário"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userController.createUser(userDTO);
        });

        assertEquals("400 BAD_REQUEST \"Erro ao criar usuário\"", exception.getMessage());
    }

    @Test
    public void testGetAllUsers() {
        List<UserDTO> users = Arrays.asList(
                new UserDTO(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON),
                new UserDTO(2L, "Jane Doe", "98765432100", "jane@example.com", "password", BigDecimal.ZERO, UserType.MERCHANT)
        );
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).nome());
        assertEquals("Jane Doe", response.getBody().get(1).nome());
    }

    @Test
    public void testGetAllUsersError() {
        when(userService.getAllUsers()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar usuários"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getAllUsers();
        });

        assertEquals("500 INTERNAL_SERVER_ERROR \"Erro ao buscar usuários\"", exception.getMessage());
    }

    @Test
    public void testGetUserById() {
        UserDTO userDTO = new UserDTO(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userService.getUserById(1L)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().nome());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    public void testGetUserByIdError() {
        when(userService.getUserById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUserById(1L);
        });

        assertEquals("404 NOT_FOUND \"Usuário não encontrado\"", exception.getMessage());
    }
}