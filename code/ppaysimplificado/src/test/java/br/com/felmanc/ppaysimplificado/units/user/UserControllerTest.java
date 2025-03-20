package br.com.felmanc.ppaysimplificado.units.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felmanc.ppaysimplificado.configs.TestConfig;
import br.com.felmanc.ppaysimplificado.controllers.UserController;
import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.services.UserService;

@WebMvcTest(UserController.class)
@Import(TestConfig.class) // Importando configuração de mock
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateUser() throws Exception {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userService.createUser(Mockito.any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("John Doe"))
                .andExpect(jsonPath("$.cpf").value("12345678900"));
    }

    @Test
    public void testGetAllUsers_NoUsersFound() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetAllUsers_UsersFound() throws Exception {
        List<UserDTO> users = Arrays.asList(
                new UserDTO(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON),
                new UserDTO(2L, "Jane Doe", "98765432100", "jane@example.com", "password", BigDecimal.ZERO, UserType.MERCHANT)
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("John Doe"))
                .andExpect(jsonPath("$[1].nome").value("Jane Doe"));
    }

    @Test
    public void testGetUserById() throws Exception {
        UserDTO userDTO = new UserDTO(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userService.getUserById(1L)).thenReturn(userDTO);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("John Doe"))
                .andExpect(jsonPath("$.id").value(1));
    }
}
