package br.com.felmanc.ppaysimplificado.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "payer.id", target = "idPagador")
    @Mapping(source = "payee.id", target = "idRecebedor")
    @Mapping(source = "value", target = "valor")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "timestamp", target = "data")
    TransactionDTO toDTO(TransactionEntity transactionEntity);

    @Mapping(source = "idPagador", target = "payer.id")
    @Mapping(source = "idRecebedor", target = "payee.id")
    @Mapping(source = "valor", target = "value")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "data", target = "timestamp")
    TransactionEntity toEntity(TransactionDTO transactionDTO);
}