package br.com.felmanc.ppaysimplificado.units.clients;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import br.com.felmanc.ppaysimplificado.clients.NotificationClientImpl;
import br.com.felmanc.ppaysimplificado.dtos.NotificationDTO;
import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NotificationClientImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @MockitoBean
    private NotificationClientImpl notificationClient;

    @Mock
    private LoggerUtil loggerUtil;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        // Create the instance manually
        notificationClient = new NotificationClientImpl(webClientBuilder, loggerUtil);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSendNotificationSuccess() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        String message = "Mensagem de teste";
        String jsonResponse = "{\"status\":\"success\"}";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(NotificationDTO.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        boolean result = notificationClient.sendNotification(user, message);

        assertTrue(result, "Esperado que a notificação fosse enviada com sucesso.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSendNotificationServerError() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        String message = "Mensagem de teste";
        WebClientResponseException serverException = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno do Servidor",
                null,
                null,
                null
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(NotificationDTO.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(serverException));

        boolean result = notificationClient.sendNotification(user, message);

        assertFalse(result, "Esperado que a notificação falhasse devido a erro no servidor.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSendNotificationGatewayTimeout() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        String message = "Mensagem de teste";
        WebClientResponseException timeoutException = WebClientResponseException.create(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Tempo de Espera do Gateway Esgotado",
                null,
                null,
                null
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(NotificationDTO.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(timeoutException));

        boolean result = notificationClient.sendNotification(user, message);

        assertFalse(result, "Esperado que a notificação falhasse devido a tempo de espera do gateway esgotado.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSendNotificationUnexpectedError() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        String message = "Mensagem de teste";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(NotificationDTO.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Erro inesperado")));

        boolean result = notificationClient.sendNotification(user, message);

        assertFalse(result, "Esperado que a notificação falhasse devido a erro inesperado.");
    }
}