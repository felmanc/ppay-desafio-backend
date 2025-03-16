package br.com.felmanc.ppaysimplificado.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Mapeia de UserEntity para UserDTO
    UserDTO toDTO(UserEntity userEntity);

    // Mapeia de UserDTO para UserEntity
    UserEntity toEntity(UserDTO userDTO);
}
