package br.com.felmanc.ppaysimplificado.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;

@Mapper(componentModel = "spring", imports = java.time.format.DateTimeFormatter.class)
public interface TransactionMapper {

    @Mapping(source = "payer.id", target = "idPagador")
    @Mapping(source = "payee.id", target = "idRecebedor")
    @Mapping(source = "value", target = "valor")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "timestamp", target = "data", dateFormat = "yyyy-MM-dd HH:mm:ss")
    TransactionDTO toDTO(TransactionEntity transactionEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "idPagador", target = "payer.id")
    @Mapping(source = "idRecebedor", target = "payee.id")
    @Mapping(source = "valor", target = "value")
    @Mapping(source = "status", target = "status", ignore = true)
    @Mapping(source = "data", target = "timestamp", ignore = true)
    TransactionEntity toEntity(TransactionDTO transactionDTO);
    
    List<TransactionDTO> toDTOList(List<TransactionEntity> transactionEntities);
}