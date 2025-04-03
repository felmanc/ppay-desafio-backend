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
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationClient notificationClient; 
    private final TransactionMapper transactionMapper;
    private final AuthorizationClient authorizationClient;
    private final LoggerUtil loggerUtil;

    public TransactionService(TransactionRepository transactionRepository, UserService userService,
            NotificationClient notificationClientImpl, TransactionMapper transactionMapper,
            AuthorizationClient authorizationClient, LoggerUtil loggerUtil) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.notificationClient = notificationClientImpl;
        this.transactionMapper = transactionMapper;
        this.authorizationClient = authorizationClient;
        this.loggerUtil = loggerUtil;
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        loggerUtil.logInfo("Transação", "Iniciando transferência de {} do usuário {} para o usuário {}",
                transactionDTO.valor(), transactionDTO.idPagador(), transactionDTO.idRecebedor());

        UserEntity payer = userService.findUserEntityById(transactionDTO.idPagador());
        loggerUtil.logInfo("Transação", "Retornado pagador: {}, Saldo inicial: {}", payer.getId(), payer.getBalance());

        UserEntity payee = userService.findUserEntityById(transactionDTO.idRecebedor());
        loggerUtil.logInfo("Transação", "Retornado recebedor: {}, Saldo inicial: {}", payee.getId(), payee.getBalance());

        validateTransaction(transactionDTO, payer, payee);

        payer.setBalance(payer.getBalance().subtract(transactionDTO.valor()));
        payee.setBalance(payee.getBalance().add(transactionDTO.valor()));

        loggerUtil.logInfo("Transação", "Saldo após débito do pagador: {}", payer.getBalance());
        loggerUtil.logInfo("Transação", "Saldo após crédito do recebedor: {}", payee.getBalance());

        TransactionEntity transaction = new TransactionEntity();
        transaction.setValue(transactionDTO.valor());
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimestamp(transactionDTO.data() != null ? transactionDTO.data() : LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        try {
            if (!authorizeTransaction(transaction)) {
                loggerUtil.logInfo("Transação", "Autorização falhou, lançando exceção...");
                transaction.setStatus(TransactionStatus.FAILED);
                throw new UnauthorizedTransactionException("Transação não autorizada pelo serviço externo.");
            }
        } catch (Exception e) {
            loggerUtil.logError("Transação", "Capturando exceção: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;
        }

        loggerUtil.logInfo("Transação", "Transação autorizada pelo serviço externo.");
        transaction.setStatus(TransactionStatus.AUTHORIZED);

        if (notificationClient.sendNotification(payer, "Transação efetuada com sucesso. ID: " + transaction.getId())) {
            loggerUtil.logInfo("Transação", "Notificação efetuada com sucesso.");
        }

        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionMapper.toDTO(transaction);
    }

    private void validateTransaction(TransactionDTO transactionDTO, UserEntity payer, UserEntity payee) {
        if (transactionDTO == null) {
            loggerUtil.logError("Validação", "O objeto TransactionDTO é nulo.");
            throw new IllegalArgumentException("O objeto TransactionDTO não pode ser nulo.");
        }

        if (transactionDTO.valor() == null || transactionDTO.valor().compareTo(BigDecimal.ZERO) <= 0) {
            loggerUtil.logError("Validação", "Valor da transação deve ser maior que zero. Valor recebido: {}", transactionDTO.valor());
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        if (payer == null) {
            loggerUtil.logError("Validação", "Pagador não encontrado. ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Pagador não encontrado.");
        }

        if (payee == null) {
            loggerUtil.logError("Validação", "Recebedor não encontrado. ID: {}", transactionDTO.idRecebedor());
            throw new IllegalArgumentException("Recebedor não encontrado.");
        }

        if (transactionDTO.idPagador().equals(transactionDTO.idRecebedor())) {
            loggerUtil.logError("Validação", "Pagador e recebedor não podem ser o mesmo.");
            throw new IllegalArgumentException("Pagador e recebedor não podem ser o mesmo.");
        }

        if (payer.getType().equals(UserType.MERCHANT)) {
            loggerUtil.logError("Validação", "Pagador não pode ser lojista. ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Pagador não pode ser lojista.");
        }

        if (payer.getBalance().compareTo(transactionDTO.valor()) < 0) {
            loggerUtil.logError("Validação", "Saldo insuficiente para o pagador com ID: {}", transactionDTO.idPagador());
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }

    private boolean authorizeTransaction(TransactionEntity transaction) {
        return authorizationClient.authorizeTransaction();
    }

    public List<TransactionDTO> getAllTransactions() {
        loggerUtil.logInfo("Consulta", "Buscando todas as transações");
        List<TransactionEntity> transactionEntities = transactionRepository.findAll();
        loggerUtil.logInfo("Consulta", "Número de transações encontradas: {}", transactionEntities.size());
        return transactionMapper.toDTOList(transactionEntities);
    }
}