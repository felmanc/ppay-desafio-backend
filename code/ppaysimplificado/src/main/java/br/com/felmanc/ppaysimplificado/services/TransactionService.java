package br.com.felmanc.ppaysimplificado.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, WebClient webClient) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.webClient = webClient;
    }

    @Transactional
    public TransactionEntity transfer(Long payerId, Long payeeId, Double value) {
        log.info("Iniciando transferência de {} do usuário {} para o usuário {}", value, payerId, payeeId);

        UserEntity payer = userRepository.findById(payerId)
                .orElseThrow(() -> {
                    log.error("Pagador não encontrado com ID: {}", payerId);
                    return new IllegalArgumentException("Pagador não encontrado.");
                });

        UserEntity payee = userRepository.findById(payeeId)
                .orElseThrow(() -> {
                    log.error("Recebedor não encontrado com ID: {}", payeeId);
                    return new IllegalArgumentException("Recebedor não encontrado.");
                });

        if (payer.getBalance() < value) {
            log.error("Saldo insuficiente para o pagador com ID: {}", payerId);
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        payer.setBalance(payer.getBalance() - value);
        payee.setBalance(payee.getBalance() + value);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setValue(value);
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setStatus(TransactionStatus.PENDING);

        transaction = transactionRepository.save(transaction);
        log.info("Transação criada com status PENDING. ID da transação: {}", transaction.getId());

        boolean authorized = authorizeTransaction();
    	if (!authorized) {
            log.error("Transação não autorizada. ID da transação: {}", transaction.getId());
    	    transaction.setStatus(TransactionStatus.FAILED);
    	    transactionRepository.save(transaction);
    	    throw new IllegalStateException("Transação não autorizada pelo serviço externo.");
    	}

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info("Transação concluída com sucesso. ID da transação: {}", transaction.getId());

        return transaction;
    }
    
	public boolean authorizeTransaction() {
	    try {
	        String response = webClient
	            .get()
	            .uri("https://util.devi.tools/api/v2/authorize")
	            .retrieve()
	            .bodyToMono(String.class)
	            .block();
	
	        // Retorna true se o serviço autorizar
	        return "AUTHORIZED".equalsIgnoreCase(response);
	    } catch (WebClientResponseException e) {
	        // Trata erros de resposta HTTP
	        throw new IllegalStateException("Erro na autorização: " + e.getStatusCode(), e);
	    } catch (Exception e) {
	        // Trata outros erros
	        throw new IllegalStateException("Erro inesperado na autorização.", e);
	    }    
	}
}
