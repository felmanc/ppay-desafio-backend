package br.com.felmanc.ppaysimplificado.units.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.mappers.UserMapper;

public class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    public void testToDTO() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("João da Silva");
        userEntity.setCpf("11122233344");
        userEntity.setEmail("joao@literatura.com.br");
        userEntity.setPassword("senha123");
        userEntity.setBalance(new BigDecimal("1000.00"));
        userEntity.setType(UserType.COMMON);

        UserDTO userDTO = userMapper.toDTO(userEntity);

        assertEquals(userEntity.getId(), userDTO.id());
        assertEquals(userEntity.getName(), userDTO.nome());
        assertEquals(userEntity.getCpf(), userDTO.cpf());
        assertEquals(userEntity.getEmail(), userDTO.email());
        assertEquals(userEntity.getPassword(), userDTO.senha());
        assertEquals(userEntity.getBalance(), userDTO.saldo());
        assertEquals(userEntity.getType(), userDTO.tipo());
    }

    @Test
    public void testToEntity() {
        UserDTO userDTO = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("1000.00"), UserType.COMMON);

        UserEntity userEntity = userMapper.toEntity(userDTO);

        assertEquals(userDTO.nome(), userEntity.getName());
        assertEquals(userDTO.cpf(), userEntity.getCpf());
        assertEquals(userDTO.email(), userEntity.getEmail());
        assertEquals(userDTO.senha(), userEntity.getPassword());
        assertEquals(userDTO.saldo(), userEntity.getBalance());
        assertEquals(userDTO.tipo(), userEntity.getType());
    }
}