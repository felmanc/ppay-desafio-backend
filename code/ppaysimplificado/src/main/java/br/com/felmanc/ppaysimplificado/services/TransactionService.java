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
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import br.com.felmanc.ppaysimplificado.mappers.TransactionMapper;
import br.com.felmanc.ppaysimplificado.repositories.TransactionRepository;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import br.com.felmanc.ppaysimplificado.validators.TransactionValidator;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationClient notificationClient;
    private final TransactionMapper transactionMapper;
    private final AuthorizationClient authorizationClient;
    private final LoggerUtil loggerUtil;
    private final UserBalanceService userBalanceService;
    private final TransactionValidator transactionValidator;

	public TransactionService(TransactionRepository transactionRepository, UserService userService,
			NotificationClient notificationClient, TransactionMapper transactionMapper,
			AuthorizationClient authorizationClient, LoggerUtil loggerUtil, UserBalanceService userBalanceService,
			TransactionValidator transactionValidator) {
		this.transactionRepository = transactionRepository;
		this.userService = userService;
		this.notificationClient = notificationClient;
		this.transactionMapper = transactionMapper;
		this.authorizationClient = authorizationClient;
		this.loggerUtil = loggerUtil;
		this.userBalanceService = userBalanceService;
		this.transactionValidator = transactionValidator;
	}

	@Transactional
	public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
	    loggerUtil.logInfo("Transação", "Iniciando transferência de {} do usuário {} para o usuário {}",
	            transactionDTO.valor(), transactionDTO.idPagador(), transactionDTO.idRecebedor());
	
	    UserEntity payer = fetchAndLogUser(transactionDTO.idPagador(), "Pagador");
	    UserEntity payee = fetchAndLogUser(transactionDTO.idRecebedor(), "Recebedor");
	
	    transactionValidator.validateTransaction(transactionDTO, payer, payee);
	
	    processTransaction(payer, payee, transactionDTO.valor());
	
	    TransactionEntity transaction = saveTransaction(transactionDTO, payer, payee);
	
	    authorizeTransaction(transaction);
	
	    notifyUsers(transaction, payer);
	
	    return transactionMapper.toDTO(transaction);
	}
	
	private UserEntity fetchAndLogUser(Long userId, String role) {
	    UserEntity user = userService.findUserEntityById(userId);
	    loggerUtil.logInfo("Transação", "Retornado {}: {}, Saldo inicial: {}", role, user.getId(), user.getBalance());
	    return user;
	}
	
	private void processTransaction(UserEntity payer, UserEntity payee, BigDecimal amount) {
	    userBalanceService.debitar(payer, amount);
	    userBalanceService.creditar(payee, amount);
	    loggerUtil.logInfo("Transação", "Saldo após débito do pagador: {}", payer.getBalance());
	    loggerUtil.logInfo("Transação", "Saldo após crédito do recebedor: {}", payee.getBalance());
	}
	
	private TransactionEntity saveTransaction(TransactionDTO transactionDTO, UserEntity payer, UserEntity payee) {
	    TransactionEntity transaction = new TransactionEntity();
	    transaction.setValue(transactionDTO.valor());
	    transaction.setPayer(payer);
	    transaction.setPayee(payee);
	    transaction.setStatus(TransactionStatus.PENDING);
	    transaction.setTimestamp(transactionDTO.data() != null ? transactionDTO.data() : LocalDateTime.now());
	    return transactionRepository.save(transaction);
	}
	
	private void authorizeTransaction(TransactionEntity transaction) {
	    if (!authorizationClient.authorizeTransaction()) {
	        loggerUtil.logInfo("Transação", "Autorização falhou, lançando exceção...");
	        transaction.setStatus(TransactionStatus.FAILED);
	        throw new UnauthorizedTransactionException("Transação não autorizada pelo serviço externo.");
	    }
	    loggerUtil.logInfo("Transação", "Transação autorizada pelo serviço externo.");
	    transaction.setStatus(TransactionStatus.AUTHORIZED);
	}
	
	private void notifyUsers(TransactionEntity transaction, UserEntity payer) {
	    if (notificationClient.sendNotification(payer, "Transação efetuada com sucesso. ID: " + transaction.getId())) {
	        loggerUtil.logInfo("Transação", "Notificação efetuada com sucesso.");
	    }
	    transaction.setStatus(TransactionStatus.COMPLETED);
	}

    public List<TransactionDTO> getAllTransactions() {
        loggerUtil.logInfo("Consulta", "Buscando todas as transações");
        List<TransactionEntity> transactionEntities = transactionRepository.findAll();
        loggerUtil.logInfo("Consulta", "Número de transações encontradas: {}", transactionEntities.size());
        return transactionMapper.toDTOList(transactionEntities);
	}
}