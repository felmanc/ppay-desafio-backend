package br.com.felmanc.ppaysimplificado.units.clients;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClientImpl;
import br.com.felmanc.ppaysimplificado.exceptions.UnauthorizedTransactionException;
import reactor.core.publisher.Mono;

public class AuthorizationClientImplTest {

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
	@Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthorizationClientImpl authorizationClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizeTransaction_Success() throws Exception {
        // Mockando resposta de sucesso
        String mockResponse = "{ \"status\": \"success\", \"data\": { \"authorization\": true } }";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        boolean result = authorizationClient.authorizeTransaction();

        // Verificação
        assertTrue(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizeTransaction_Failure() throws Exception {
        // Mockando resposta de falha
        String mockResponse = "{ \"status\": \"success\", \"data\": { \"authorization\": false } }";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

        boolean result = authorizationClient.authorizeTransaction();

        // Verificação
        assertFalse(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizeTransaction_Forbidden() {
        // Mockando erro de acesso proibido
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenThrow(
            new WebClientResponseException(HttpStatus.FORBIDDEN.value(), "Forbidden", null, null, null)
        );

        // Verificação
        Exception exception = assertThrows(UnauthorizedTransactionException.class, () -> {
            authorizationClient.authorizeTransaction();
        });
        assertEquals("Transação não autorizada.", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizeTransaction_InternalServerError() {
        // Mockando erro de servidor interno
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenThrow(
            new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)
        );

        // Verificação
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authorizationClient.authorizeTransaction();
        });
        assertTrue(exception.getMessage().contains("Erro inesperado na autorização."));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizeTransaction_UnexpectedError() {
        // Mockando erro inesperado (RuntimeException genérica)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenThrow(new RuntimeException("Erro genérico inesperado"));

        // Verificação
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authorizationClient.authorizeTransaction();
        });
        assertTrue(exception.getMessage().contains("Erro inesperado na autorização."));
    } 
}