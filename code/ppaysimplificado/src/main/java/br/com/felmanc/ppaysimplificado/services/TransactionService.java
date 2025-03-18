package br.com.felmanc.ppaysimplificado.services;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import br.com.felmanc.ppaysimplificado.mappers.TransactionMapper;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, WebClient webClient, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.webClient = webClient;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionDTO transfer(TransactionDTO transactionDTO) {
        log.info("Iniciando transferência de {} do usuário {} para o usuário {}",
                transactionDTO.getValor(), transactionDTO.getIdPagador(), transactionDTO.getIdRecebedor());

        if(transactionDTO.getIdPagador().equals(transactionDTO.getIdRecebedor()))
        {
            log.error("Pagador e recebedor não podem ser o mesmo.");
            throw new IllegalArgumentException("Pagador e recebedor não podem ser o mesmo.");
        }
        
        UserEntity payer = userRepository.findById(transactionDTO.getIdPagador())
                .orElseThrow(() -> {
                    log.error("Pagador não encontrado com ID: {}.", transactionDTO.getIdPagador());
                    return new IllegalArgumentException("Pagador não encontrado.");
                });

        if(payer.getType().equals(UserType.MERCHANT))
        {
            log.error("Pagador não pode ser lojista. ID: {}", transactionDTO.getIdPagador());
            throw new IllegalArgumentException("Pagador não pode ser lojista.");
        }
        
        UserEntity payee = userRepository.findById(transactionDTO.getIdRecebedor())
                .orElseThrow(() -> {
                    log.error("Recebedor não encontrado com ID: {}", transactionDTO.getIdRecebedor());
                    return new IllegalArgumentException("Recebedor não encontrado.");
                });

        if (payer.getBalance() < transactionDTO.getValor()) {
            log.error("Saldo insuficiente para o pagador com ID: {}", transactionDTO.getIdPagador());
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        payer.setBalance(payer.getBalance() - transactionDTO.getValor());
        payee.setBalance(payee.getBalance() + transactionDTO.getValor());

        TransactionEntity transaction = new TransactionEntity();
        transaction.setValue(transactionDTO.getValor());
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setStatus(TransactionStatus.PENDING);

        // Definir a data atual se não fornecida
        if (transactionDTO.getData() == null) {
            transactionDTO.setData(LocalDateTime.now());
        }
        transaction.setTimestamp(transactionDTO.getData());

        transaction = transactionRepository.save(transaction);

        boolean authorized = authorizeTransaction(transaction);
        if (!authorized) {
            transactionRepository.save(transaction);
            throw new UnauthorizedTransactionException("Transação não autorizada pelo serviço externo.");
        }
        
        log.info("Transação autorizada pelo serviço externo.");
        
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        return transactionMapper.toDTO(transaction);
    }

    public boolean authorizeTransaction(TransactionEntity transaction) {
        try {
            String response = webClient
                    .get()
                    .uri("https://util.devi.tools/api/v2/authorize")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();
            boolean authorization = jsonNode.path("data").path("authorization").asBoolean();

            if ("success".equalsIgnoreCase(status)) {
                transaction.setStatus(TransactionStatus.AUTHORIZED);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
            }

            return authorization;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new UnauthorizedTransactionException("Acesso não autorizado. Verifique suas credenciais.");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado na autorização.", e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro inesperado na autorização.", e);
        }
    }
}
