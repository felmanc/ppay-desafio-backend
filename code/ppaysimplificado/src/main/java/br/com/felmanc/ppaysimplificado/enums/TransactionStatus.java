package br.com.felmanc.ppaysimplificado.enums;

public enum TransactionStatus {
    PENDING,     // Transação pendente
    AUTHORIZED,  // Autorizada pelo serviço externo
    COMPLETED,   // Finalizada com sucesso
    FAILED       // Falha na transação
}
