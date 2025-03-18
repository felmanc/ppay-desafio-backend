package br.com.felmanc.ppaysimplificado.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "nome", source = "name")
    @Mapping(target = "senha", source = "password")
    @Mapping(target = "saldo", source = "balance")
    @Mapping(target = "tipo", source = "type")
    UserDTO toDTO(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "password", source = "senha")
    @Mapping(target = "balance", source = "saldo")
    @Mapping(target = "type", source = "tipo")
    UserEntity toEntity(UserDTO userDTO);
}