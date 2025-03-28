package br.com.felmanc.ppaysimplificado.clients;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;

@Component
public class AuthorizationClientImpl implements AuthorizationClient {

    private final WebClient webClient;

    public AuthorizationClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://util.devi.tools").build();
    }

    @Override
    public boolean authorizeTransaction() {
        try {
            String response = webClient.get()
                .uri("/api/v2/authorize")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();
            boolean authorization = jsonNode.path("data").path("authorization").asBoolean();

            return "success".equalsIgnoreCase(status) && authorization;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new UnauthorizedTransactionException("Transação não autorizada.");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado na autorização.", e);
    }
        } catch (Exception e) {
            throw new IllegalStateException("Erro inesperado na autorização.");
        }
    }
}