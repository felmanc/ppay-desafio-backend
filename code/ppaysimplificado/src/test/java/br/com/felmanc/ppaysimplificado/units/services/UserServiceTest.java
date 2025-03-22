package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import br.com.felmanc.ppaysimplificado.services.UserService;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void testGetAllUsersWithUsers() {
        UserEntity userEntity = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(userEntity));
        when(userMapper.toDTO(any(UserEntity.class))).thenReturn(new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON));

        List<UserDTO> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllUsersWithoutUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDTO> users = userService.getAllUsers();
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserByIdValid() {
        UserEntity userEntity = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
        when(userMapper.toDTO(any(UserEntity.class))).thenReturn(new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON));

        UserDTO user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals("John Doe", user.nome());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testGetUserByIdInvalid() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(1L);
        });

        String expectedMessage = "Usuário com o ID 1 não foi encontrado.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testCreateUserCommon() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);

        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toDTO(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.nome());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserMerchant() {
        UserDTO userDTO = new UserDTO(null, "Jane Doe", "98765432100", "jane@example.com", "password", BigDecimal.ZERO, UserType.MERCHANT);
        UserEntity userEntity = new UserEntity(null, "Jane Doe", "98765432100", "jane@example.com", "password", BigDecimal.ZERO, UserType.MERCHANT);

        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toDTO(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals("Jane Doe", createdUser.nome());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserWithDuplicateCpf() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);

        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        String expectedMessage = "Já existe um usuário com este CPF.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserWithDuplicateEmail() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);

        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        String expectedMessage = "Já existe um usuário com este e-mail.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserWithNegativeBalance() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", new BigDecimal("-100.0"), UserType.COMMON);
        UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password", new BigDecimal("-100.0"), UserType.COMMON);

        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toDTO(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.nome());
        assertEquals(new BigDecimal("-100.0"), createdUser.saldo());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    
    @Test
    public void testGetAllUsersWithMultipleUsers() {
        UserEntity user1 = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        UserEntity user2 = new UserEntity(2L, "Jane Doe", "98765432100", "jane@example.com", "password", BigDecimal.valueOf(100.0), UserType.MERCHANT);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDTO(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return new UserDTO(null, entity.getName(), entity.getCpf(), entity.getEmail(), entity.getPassword(), entity.getBalance(), entity.getType());
        });

        List<UserDTO> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testCreateUserWithInvalidCpf() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "invalid_cpf", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(userDTO);
        });

        String expectedMessage = "O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserWithInvalidEmail() {
        UserDTO userDTO = new UserDTO(null, "John Doe", "12345678900", "invalid_email", "password", BigDecimal.ZERO, UserType.COMMON);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(userDTO);
        });

        String expectedMessage = "O e-mail deve estar em um formato válido.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    public void testCreateUserWithoutName() {
        UserDTO userWithoutName = new UserDTO(null, null, "12345678900", "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        Exception exceptionName = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userWithoutName);
        });
        assertTrue(exceptionName.getMessage().contains("O nome do usuário é obrigatório."));
    }

    @Test
    public void testCreateUserWithoutCpf() {
        UserDTO userWithoutCpf = new UserDTO(null, "John Doe", null, "john@example.com", "password", BigDecimal.ZERO, UserType.COMMON);
        Exception exceptionCpf = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userWithoutCpf);
        });
        assertTrue(exceptionCpf.getMessage().contains("O CPF/ CNPJ é obrigatório."));
    }

    @Test
    public void testCreateUserWithoutEmail() {
        UserDTO userWithoutEmail = new UserDTO(null, "John Doe", "12345678900", null, "password", BigDecimal.ZERO, UserType.COMMON);
        Exception exceptionEmail = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userWithoutEmail);
        });
        assertTrue(exceptionEmail.getMessage().contains("O e-mail é obrigatório."));
    }

    @Test
    public void testCreateUserWithoutPassword() {
        UserDTO userWithoutPassword = new UserDTO(null, "John Doe", "12345678900", "john@example.com", null, BigDecimal.ZERO, UserType.COMMON);
        Exception exceptionPassword = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userWithoutPassword);
        });
        assertTrue(exceptionPassword.getMessage().contains("A senha é obrigatória."));
    }

    @Test
    public void testCreateUserWithoutType() {
        UserDTO userWithoutType = new UserDTO(null, "John Doe", "12345678900", "john@example.com", "password", BigDecimal.ZERO, null);
        Exception exceptionType = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userWithoutType);
        });
        assertTrue(exceptionType.getMessage().contains("O tipo do usuário (COMMON ou MERCHANT) é obrigatório."));
    }
}