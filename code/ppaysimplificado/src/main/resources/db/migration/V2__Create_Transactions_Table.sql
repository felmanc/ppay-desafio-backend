CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `value` DECIMAL(19,2) NOT NULL,
    `payer_id` BIGINT NOT NULL,
    `payee_id` BIGINT NOT NULL,
    `status` ENUM('AUTHORIZED', 'COMPLETED', 'FAILED', 'PENDING') NOT NULL,
    `timestamp` DATETIME(6) NOT NULL,
    CONSTRAINT `fk_payer` FOREIGN KEY (`payer_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_payee` FOREIGN KEY (`payee_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;