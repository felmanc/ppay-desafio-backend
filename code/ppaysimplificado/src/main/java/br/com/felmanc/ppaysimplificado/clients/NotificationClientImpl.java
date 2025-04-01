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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NotificationClientImpl implements NotificationClient {

    private final WebClient webClient;

    public NotificationClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://util.devi.tools/api/v1").build();
    }

    @Override
    public boolean sendNotification(UserEntity user, String message) {
        log.info("[Notificação] Iniciando envio de notificação para o email: {}", user.getEmail());
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
                            log.error("[Erro] Erro no servidor ao enviar notificação.");
                            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no servidor."));
                        } else if (statusCode.equals(HttpStatusCode.valueOf(504))) {
                            log.error("[Erro] Timeout no gateway ao enviar notificação.");
                            return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Timeout do gateway."));
                        } else {
                            log.warn("[Aviso] Erro inesperado: {}", statusCode);
                            return clientResponse.createException();
                        }
                    })
                .bodyToMono(String.class)
                .block();

            log.debug("[Notificação] Resposta do serviço: {}", response);

            if (response == null && HttpStatus.NO_CONTENT.value() == 204) {
                log.info("[Notificação] Envio concluído com sucesso ("+ HttpStatus.NO_CONTENT.value() + " - No Content).");
                return true;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();
            log.debug("[Notificação] Status retornado pela API: {}", status);

            if ("success".equalsIgnoreCase(status)) {
                log.info("[Notificação] Notificação enviada com sucesso.");
                return true;
            } else {
                log.warn("[Aviso] Notificação não foi enviada com sucesso, status: {}", status);
                return false;
            }
        } catch (Exception e) {
            log.error("[Erro] Exceção ao enviar notificação para o email: {}", user.getEmail(), e);
            return false;
        }
    }
}
