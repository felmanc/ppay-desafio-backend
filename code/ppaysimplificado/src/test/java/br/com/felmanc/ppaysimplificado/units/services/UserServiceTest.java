package br.com.felmanc.ppaysimplificado.units.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import br.com.felmanc.ppaysimplificado.services.UserService;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import br.com.felmanc.ppaysimplificado.validators.UserValidator;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	@Mock
	private LoggerUtil loggerUtil;

	@Mock
	private UserValidator userValidator;

	private UserDTO criarUserDTO(Long id, String nome, String cpf, String email, String senha, BigDecimal saldo, UserType tipo) {
		return new UserDTO(id, nome, cpf, email, senha, saldo, tipo);
	}

	private UserDTO criarUserDTOObrigatorios(String nome, String cpf, String email, String senha, UserType tipo) {
		return criarUserDTO(null, nome, cpf, email, senha, null, tipo);
	}

	private UserDTO criarEConfigurarValidador(String nome, String cpf, String email, String senha, UserType tipo) {
		UserDTO userDTO = criarUserDTOObrigatorios(nome, cpf, email, senha, tipo);
		configurarValidadorRealComDTO(userDTO);
		return userDTO;
	}

	private UserDTO criarEConfigurarValidador(String nome, String cpf, String email, String senha, BigDecimal saldo, UserType tipo) {
		UserDTO userDTO = criarUserDTO(null, nome, cpf, email, senha, saldo, tipo);
		configurarValidadorRealComDTO(userDTO);
		return userDTO;
	}

	private void configurarValidadorRealComDTO(UserDTO userDTO) {
		doCallRealMethod().when(userValidator).validarDadosUsuario(
				eq(userDTO.nome()), eq(userDTO.cpf()),
				eq(userDTO.email()), eq(userDTO.senha()),
				eq(userDTO.tipo())
		);
	}

	@Test
	void testGetAllUsersWithUsers() {
		UserEntity userEntity = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password",
				BigDecimal.ZERO, UserType.COMMON);
		when(userRepository.findAll()).thenReturn(Collections.singletonList(userEntity));
		when(userMapper.toDTO(any(UserEntity.class))).thenReturn(criarUserDTO(null, "John Doe", "12345678900",
				"john@example.com", "password", BigDecimal.ZERO, UserType.COMMON));

		List<UserDTO> users = userService.getAllUsers();
		assertFalse(users.isEmpty());
		assertEquals(1, users.size());
		verify(userRepository, times(1)).findAll();
		verify(userRepository, times(0)).save(any());
	}

	@Test
	void testGetAllUsersWithoutUsers() {
		when(userRepository.findAll()).thenReturn(Collections.emptyList());

		List<UserDTO> users = userService.getAllUsers();
		assertTrue(users.isEmpty());
		verify(userRepository, times(1)).findAll();
		verify(userRepository, times(0)).save(any());
	}

	@Test
	void testGetUserByIdValid() {
		UserEntity userEntity = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password",
				BigDecimal.ZERO, UserType.COMMON);
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
		when(userMapper.toDTO(any(UserEntity.class))).thenReturn(criarUserDTO(null, "John Doe", "12345678900",
				"john@example.com", "password", BigDecimal.ZERO, UserType.COMMON));

		UserDTO user = userService.getUserById(1L);
		assertNotNull(user);
		assertEquals("John Doe", user.nome());
		verify(userRepository, times(1)).findById(anyLong());
		verify(userRepository, times(0)).save(any());
	}

	@Test
	void testGetUserByIdInvalid() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.getUserById(1L);
		});

		String expectedMessage = "Usuário com o ID 1 não foi encontrado.";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
		verify(userRepository, times(1)).findById(anyLong());
		verify(userRepository, times(0)).save(any());
	}

	@Test
	void testCreateUserCommon() {
		UserDTO userDTO = criarEConfigurarValidador("John Doe", "12345678900", "john@example.com", "password", UserType.COMMON);
		UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password",
				BigDecimal.ZERO, UserType.COMMON);

		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
		when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		when(userMapper.toDTO(any(UserEntity.class))).thenReturn(userDTO);

		UserDTO createdUser = userService.createUser(userDTO);

		assertNotNull(createdUser);
		assertEquals("John Doe", createdUser.nome());
		verify(userRepository, times(1)).save(any(UserEntity.class));
		verify(userRepository, times(1)).findByCpf(anyString());
		verify(userRepository, times(1)).findByEmail(anyString());
	}

	@Test
	void testCreateUserMerchant() {
		UserDTO userDTO = criarEConfigurarValidador("Jane Doe", "98765432100", "jane@example.com", "password", UserType.MERCHANT);
		UserEntity userEntity = new UserEntity(null, "Jane Doe", "98765432100", "jane@example.com", "password",
				BigDecimal.ZERO, UserType.MERCHANT);

		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
		when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		when(userMapper.toDTO(any(UserEntity.class))).thenReturn(userDTO);

		UserDTO createdUser = userService.createUser(userDTO);

		assertNotNull(createdUser);
		assertEquals("Jane Doe", createdUser.nome());
		verify(userRepository, times(1)).save(any(UserEntity.class));
		verify(userRepository, times(1)).findByCpf(anyString());
		verify(userRepository, times(1)).findByEmail(anyString());
	}

	@Test
	void testCreateUserWithDuplicateCpf() {
		UserDTO userDTO = criarEConfigurarValidador("John Doe", "12345678900", "john@example.com", "password", UserType.COMMON);
		UserEntity userEntity = new UserEntity(null, "Jane Smith", "12345678900", "jane@example.com", "differentPassword",
				BigDecimal.ZERO, UserType.MERCHANT);

		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
		when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(userEntity));

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.createUser(userDTO);
		});

		String expectedMessage = "Já existe um usuário com este CPF.";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
		verify(userRepository, times(0)).save(any(UserEntity.class));
		verify(userRepository, times(1)).findByCpf(anyString());
		verify(userRepository, times(0)).findByEmail(anyString());
	}

	@Test
	void testCreateUserWithDuplicateEmail_Behavioral() {
		UserDTO userDTO = criarEConfigurarValidador("John Doe", "12345678900", "john@example.com", "password", UserType.COMMON);
		UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password",
				BigDecimal.ZERO, UserType.COMMON);

		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(userEntity);
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
		when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.createUser(userDTO);
		});

		String expectedMessage = "Já existe um usuário com este e-mail.";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));

		verify(userRepository, never()).save(any(UserEntity.class));

		verify(userRepository, times(1)).findByCpf(anyString());
		verify(userRepository, times(1)).findByEmail(anyString());
	}

	@Test
	void testCreateUserWithNegativeBalance() {
		UserDTO userDTO = criarEConfigurarValidador("John Doe", "12345678900", "john@example.com", "password", new BigDecimal("-100.0"), UserType.COMMON);
		UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password",
				new BigDecimal("-100.0"), UserType.COMMON);

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
		verify(userRepository, times(1)).findByCpf(anyString());
		verify(userRepository, times(1)).findByEmail(anyString());
	}

	@Test
	void testGetAllUsersWithMultipleUsers() {
		UserEntity user1 = new UserEntity(1L, "John Doe", "12345678900", "john@example.com", "password",
				BigDecimal.ZERO, UserType.COMMON);
		UserEntity user2 = new UserEntity(2L, "Jane Doe", "98765432100", "jane@example.com", "password",
				BigDecimal.valueOf(100.0), UserType.MERCHANT);
		when(userRepository.findAll()).thenReturn(List.of(user1, user2));
		when(userMapper.toDTO(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity entity = invocation.getArgument(0);
			return criarUserDTO(null, entity.getName(), entity.getCpf(), entity.getEmail(), entity.getPassword(),
					entity.getBalance(), entity.getType());
		});

		List<UserDTO> users = userService.getAllUsers();
		assertFalse(users.isEmpty());
		assertEquals(2, users.size());
		verify(userRepository, times(1)).findAll();
		verify(userRepository, times(0)).save(any());
	}

    @Test
    void testCreateUserDatabaseError() {
        UserDTO userDTO = criarEConfigurarValidador("John Doe", "12345678900", "john@example.com", "password", UserType.COMMON);
        UserEntity userEntity = new UserEntity(null, "John Doe", "12345678900", "john@example.com", "password",
                BigDecimal.ZERO, UserType.COMMON);

        when(userMapper.toEntity(any())).thenReturn(userEntity);
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenThrow(new RuntimeException("Erro ao salvar no banco de dados"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(userDTO);
        });

        assertTrue(exception.getMessage().contains("Erro ao salvar no banco de dados"));
        verify(userRepository, times(1)).save(any());
    }
}