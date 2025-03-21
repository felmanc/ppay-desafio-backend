package br.com.felmanc.ppaysimplificado.clients;

import org.springframework.http.HttpStatus;
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

    @Override
    public boolean sendNotification(UserEntity user, String message) {

        try {
            NotificationDTO notification = new NotificationDTO(user.getEmail(), message);

            String response = WebClient.create()
                .post()
                .uri("https://util.devi.tools/api/v1/notify")
                .bodyValue(notification)
                .retrieve()
                .onStatus(
        	    status -> status.is4xxClientError() || status.is5xxServerError(),
        	    clientResponse -> {
        	        if (clientResponse.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
        	            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no servidor."));
        	        } else if (clientResponse.statusCode().equals(HttpStatus.GATEWAY_TIMEOUT)) {
        	            return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Timeout do gateway."));
        	        } else {
        	            return clientResponse.createException();
        	        }
                })
                .bodyToMono(String.class)
                .block();

            if (response == null && HttpStatus.NO_CONTENT.value() == 204) {
                System.out.println("Notificação enviada com sucesso (204 - No Content).");
                return true;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();

            if ("success".equalsIgnoreCase(status)) {
                return true;
            } else {
                return false;
            }
        /*} catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new UnauthorizedTransactionException("Acesso não autorizado. Verifique suas credenciais.");
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no servidor.", e);
            } else if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Timeout do gateway.", e);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado.", e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro inesperado ao processar a notificação.", e);
        }*/
        } catch (Exception e) {
            log.error("Erro ao enviar notificação", e);
            return false;
        }
    }
}
