package br.com.felmanc.ppaysimplificado.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(UserEntity userEntity);

    @Mapping(target = "password", ignore = true)
    UserEntity toEntity(UserDTO userDTO);
}
