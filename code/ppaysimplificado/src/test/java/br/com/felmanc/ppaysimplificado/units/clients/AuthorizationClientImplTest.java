package br.com.felmanc.ppaysimplificado.units.clients;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClientImpl;
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class AuthorizationClientImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AuthorizationClientImpl authorizationClient;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        // Configurando o comportamento do WebClient.Builder
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // Configuração do comportamento do WebClient
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        // Inicializa a instância manualmente
        authorizationClient = new AuthorizationClientImpl(webClientBuilder);
    }

    @Test
    void testAuthorizeTransaction_Success() {
        // Simulação de resposta bem-sucedida
        String mockResponse = "{ \"status\": \"success\", \"data\": { \"authorization\": true } }";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        boolean result = authorizationClient.authorizeTransaction();

        // Verificação
        assertTrue(result);
    }

    @Test
    void testAuthorizeTransaction_Failure() {
        // Mockando resposta de falha
        String mockResponse = "{ \"status\": \"success\", \"data\": { \"authorization\": false } }";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        boolean result = authorizationClient.authorizeTransaction();

        // Verificação
        assertFalse(result);
    }

    @Test
    void testAuthorizeTransaction_Forbidden() {
        // Mockando erro de acesso proibido
        when(responseSpec.bodyToMono(String.class)).thenThrow(
            new WebClientResponseException(HttpStatus.FORBIDDEN.value(), "Forbidden", null, null, null)
        );

        Exception exception = assertThrows(UnauthorizedTransactionException.class, () -> {
            authorizationClient.authorizeTransaction();
        });

        assertEquals("Transação não autorizada.", exception.getMessage());
    }

    @Test
    void testAuthorizeTransaction_InternalServerError() {
        // Mockando erro de servidor interno
        when(responseSpec.bodyToMono(String.class)).thenThrow(
            new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authorizationClient.authorizeTransaction();
        });

        assertTrue(exception.getMessage().contains("Erro inesperado na autorização."));
    }

    @Test
    void testAuthorizeTransaction_UnexpectedError() {
        // Mockando erro inesperado (RuntimeException genérica)
        when(responseSpec.bodyToMono(String.class)).thenThrow(new RuntimeException("Erro genérico inesperado"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authorizationClient.authorizeTransaction();
        });

        assertTrue(exception.getMessage().contains("Erro inesperado na autorização."));
    }
}
