package br.com.felmanc.ppaysimplificado.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import br.com.felmanc.ppaysimplificado.repositories.UserRepository;

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
        // Verifica se os usuários existem
        Optional<UserEntity> payerOpt = userRepository.findById(payerId);
        Optional<UserEntity> payeeOpt = userRepository.findById(payeeId);

        if (payerOpt.isEmpty() || payeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Pagador ou recebedor não encontrado.");
        }

        UserEntity payer = payerOpt.get();
        UserEntity payee = payeeOpt.get();

        // Valida saldo do pagador
        if (payer.getBalance() < value) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        // Atualiza saldos
        payer.setBalance(payer.getBalance() - value);
        payee.setBalance(payee.getBalance() + value);

        // Cria a transação
        TransactionEntity transaction = new TransactionEntity();
        transaction.setValue(value);
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setStatus(TransactionStatus.PENDING);

        // Salva a transação como pendente
        transaction = transactionRepository.save(transaction);

        // Simula autorização de serviço externo
        boolean authorized = authorizeTransaction();
    	if (!authorized) {
    	    transaction.setStatus(TransactionStatus.FAILED);
    	    transactionRepository.save(transaction); // Salva como falhada
    	    throw new IllegalStateException("Transação não autorizada pelo serviço externo.");
    	}

        // Atualiza o status para COMPLETED
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

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
