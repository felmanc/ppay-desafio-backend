package br.com.felmanc.ppaysimplificado.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClient;
import br.com.felmanc.ppaysimplificado.clients.NotificationClient;
import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.entities.TransactionEntity;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import br.com.felmanc.ppaysimplificado.mappers.TransactionMapper;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationClient notificationClient; 
    private final TransactionMapper transactionMapper;
    private final AuthorizationClient authorizationClient;

	public TransactionService(TransactionRepository transactionRepository, UserService userService,
			NotificationClient notificationClientImpl, TransactionMapper transactionMapper,
			AuthorizationClient authorizationClient) {
		this.transactionRepository = transactionRepository;
		this.userService = userService;
		this.notificationClient = notificationClientImpl;
		this.transactionMapper = transactionMapper;
		this.authorizationClient = authorizationClient;
	}

	@Transactional
	public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
	    log.info("Iniciando transferência de {} do usuário {} para o usuário {}",
	            transactionDTO.valor(), transactionDTO.idPagador(), transactionDTO.idRecebedor());

	    UserEntity payer = userService.findUserEntityById(transactionDTO.idPagador());
	    log.info("Retornado pagador: {}, Saldo inicial: {}", payer.getId(), payer.getBalance());

	    UserEntity payee = userService.findUserEntityById(transactionDTO.idRecebedor());
	    log.info("Retornado recebedor: {}, Saldo inicial: {}", payee.getId(), payee.getBalance());

	    validateTransaction(transactionDTO, payer, payee);

	    payer.setBalance(payer.getBalance().subtract(transactionDTO.valor()));
	    payee.setBalance(payee.getBalance().add(transactionDTO.valor()));

	    log.info("Saldo após débito do pagador: {}", payer.getBalance());
	    log.info("Saldo após crédito do recebedor: {}", payee.getBalance());

	    TransactionEntity transaction = new TransactionEntity();
	    transaction.setValue(transactionDTO.valor());
	    transaction.setPayer(payer);
	    transaction.setPayee(payee);
	    transaction.setStatus(TransactionStatus.PENDING);
	    transaction.setTimestamp(transactionDTO.data() != null ? transactionDTO.data() : LocalDateTime.now());

	    transaction = transactionRepository.save(transaction);

	    try {
	        if (!authorizeTransaction(transaction)) {
	            log.info("Autorização falhou, lançando exceção...");
	            transaction.setStatus(TransactionStatus.FAILED);
	            throw new UnauthorizedTransactionException("Transação não autorizada pelo serviço externo.");
	        }
	    } catch (Exception e) {
	        log.info("Capturando exceção: {}", e.getMessage());
	        transaction.setStatus(TransactionStatus.FAILED);
	        throw e;
	    }

	    log.info("Transação autorizada pelo serviço externo.");
	    transaction.setStatus(TransactionStatus.AUTHORIZED);

	    if (notificationClient.sendNotification(payer, "Transação efetuada com sucesso. ID: " + transaction.getId())) {
	        log.info("Notificação efetuada com sucesso.");
	    }

	    transaction.setStatus(TransactionStatus.COMPLETED);

	    return transactionMapper.toDTO(transaction);
	}

    private void validateTransaction(TransactionDTO transactionDTO, UserEntity payer, UserEntity payee) {
        if (transactionDTO == null) {
            log.error("O objeto TransactionDTO é nulo.");
            throw new IllegalArgumentException("O objeto TransactionDTO não pode ser nulo.");
        }

        if (transactionDTO.valor() == null || transactionDTO.valor().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Valor da transação deve ser maior que zero. Valor recebido: {}", transactionDTO.valor());
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }
        
        if (payer == null) {
            log.error("Pagador não encontrado. ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Pagador não encontrado.");
        }

        if (payee == null) {
            log.error("Recebedor não encontrado. ID: {}", transactionDTO.idRecebedor());
            throw new IllegalArgumentException("Recebedor não encontrado.");
        }

        if (transactionDTO.idPagador().equals(transactionDTO.idRecebedor())) {
            log.error("Pagador e recebedor não podem ser o mesmo.");
            throw new IllegalArgumentException("Pagador e recebedor não podem ser o mesmo.");
        }

        if (payer.getType().equals(UserType.MERCHANT)) {
            log.error("Pagador não pode ser lojista. ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Pagador não pode ser lojista.");
        }

        if (payer.getBalance().compareTo(transactionDTO.valor()) < 0) {
            log.error("Saldo insuficiente para o pagador com ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }

    private boolean authorizeTransaction(TransactionEntity transaction) {
        return authorizationClient.authorizeTransaction();
    }
    
    public List<TransactionDTO> getAllTransactions() {
        log.info("Buscando todas as transações");
        List<TransactionEntity> transactionEntities = transactionRepository.findAll();
        log.info("Número de usuários encontrados: {}", transactionEntities.size());
        return transactionMapper.toDTOList(transactionEntities);
    }
}
