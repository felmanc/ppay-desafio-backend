package br.com.felmanc.ppaysimplificado.clients;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felmanc.ppaysimplificado.dtos.NotificationDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import reactor.core.publisher.Mono;

@Component
public class NotificationClientImpl implements NotificationClient {

    private final WebClient webClient;
    private final LoggerUtil loggerUtil;


    public NotificationClientImpl(WebClient.Builder webClientBuilder, LoggerUtil loggerUtil) {
        this.webClient = webClientBuilder.baseUrl("https://util.devi.tools/api/v1").build();
        this.loggerUtil = loggerUtil;
    }

    @Override
    public boolean sendNotification(UserEntity user, String message) {
        loggerUtil.logInfo("Notificação", "Iniciando envio de notificação para o email: {}", user.getEmail());
        try {
            NotificationDTO notification = new NotificationDTO(user.getEmail(), message);

            String response = webClient.post()
                .uri("/notify")
                .bodyValue(notification)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> {
                        HttpStatusCode statusCode = clientResponse.statusCode();
                        if (statusCode.equals(HttpStatusCode.valueOf(500))) {
                            loggerUtil.logError("Erro", "Erro no servidor ao enviar notificação.");
                            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no servidor."));
                        } else if (statusCode.equals(HttpStatusCode.valueOf(504))) {
                            loggerUtil.logError("Erro", "Timeout no gateway ao enviar notificação.");
                            return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Timeout do gateway."));
                        } else {
                            loggerUtil.logWarn("Aviso", "Erro inesperado: {}", statusCode);
                            return clientResponse.createException();
                        }
                    })
                .bodyToMono(String.class)
                .block();

            loggerUtil.logDebug("Notificação", "Resposta do serviço: {}", response);

            if (response == null && HttpStatus.NO_CONTENT.value() == 204) {
                loggerUtil.logInfo("Notificação", "Envio concluído com sucesso ("+ HttpStatus.NO_CONTENT.value() + " - No Content).");
                return true;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();
            loggerUtil.logDebug("Notificação", "Status retornado pela API: {}", status);

            if ("success".equalsIgnoreCase(status)) {
                loggerUtil.logInfo("Notificação", "Notificação enviada com sucesso.");
                return true;
            } else {
                loggerUtil.logWarn("Aviso", "Notificação não foi enviada com sucesso, status: {}", status);
                return false;
            }
        } catch (Exception e) {
            loggerUtil.logError("Erro", "Exceção ao enviar notificação para o email: {}", user.getEmail(), e);
            return false;
        }
    }
}