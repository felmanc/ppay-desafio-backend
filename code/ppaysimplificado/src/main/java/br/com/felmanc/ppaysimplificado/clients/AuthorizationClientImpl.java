package br.com.felmanc.ppaysimplificado.clients;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

@Component
public class AuthorizationClientImpl implements AuthorizationClient {

    private final WebClient webClient;
    private final LoggerUtil loggerUtil;

    public AuthorizationClientImpl(WebClient.Builder webClientBuilder, LoggerUtil loggerUtil) {
        this.webClient = webClientBuilder.baseUrl("https://util.devi.tools").build();
        this.loggerUtil = loggerUtil;
    }

    @Override
    public boolean authorizeTransaction() {
        try {
            loggerUtil.logInfo("Autorização", "Iniciando solicitação de autorização de transação.");
            
            String response = webClient.get()
                .uri("/api/v2/authorize")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            loggerUtil.logDebug("Autorização", "Resposta recebida da API de autorização: {}", response);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.path("status").asText();
            boolean authorization = jsonNode.path("data").path("authorization").asBoolean();

            loggerUtil.logInfo("Autorização", "Status da resposta: {}, Autorização: {}", status, authorization);

            return "success".equalsIgnoreCase(status) && authorization;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                loggerUtil.logError("Autorização", "Transação não autorizada: {}", e.getMessage());
                throw new UnauthorizedTransactionException("Transação não autorizada.");
            } else {
                loggerUtil.logError("Autorização", "Erro inesperado na autorização: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado na autorização.", e);
            }
        } catch (Exception e) {
            loggerUtil.logError("Autorização", "Erro inesperado na autorização: {}", e.getMessage());
            throw new IllegalStateException("Erro inesperado na autorização.");
        }
    }
}