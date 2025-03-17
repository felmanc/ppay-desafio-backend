package br.com.felmanc.ppaysimplificado.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "payee.id", target = "payeeId")
    TransactionDTO toDTO(TransactionEntity transactionEntity);

    @Mapping(source = "payerId", target = "payer.id")
    @Mapping(source = "payeeId", target = "payee.id")
    TransactionEntity toEntity(TransactionDTO transactionDTO);
}
