package br.com.felmanc.ppaysimplificado.mappers;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    // Mapeia de TransactionEntity para TransactionDTO
    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "payee.id", target = "payeeId")
    TransactionDTO toDTO(TransactionEntity transactionEntity);

    // Mapeia de TransactionDTO para TransactionEntity
    @Mapping(source = "payerId", target = "payer.id")
    @Mapping(source = "payeeId", target = "payee.id")
    TransactionEntity toEntity(TransactionDTO transactionDTO);
}
