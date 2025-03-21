package br.com.felmanc.ppaysimplificado.clients;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;

public interface NotificationClient {
    boolean sendNotification(UserEntity user, String message);
}
