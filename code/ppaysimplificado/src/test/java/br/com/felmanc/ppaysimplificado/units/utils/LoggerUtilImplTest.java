package br.com.felmanc.ppaysimplificado.units.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import br.com.felmanc.ppaysimplificado.utils.LoggerUtilImpl;

@ExtendWith(MockitoExtension.class)
class LoggerUtilImplTest {

    @Mock
    private Logger log;

    @InjectMocks
    private LoggerUtilImpl loggerUtil;

    private final String CATEGORY = "TESTE";
    private final String MESSAGE = "Mensagem de teste.";
    private final Object PARAM1 = "param1";
    private final Object PARAM2 = 123;
    private final String EXCEPTION_MESSAGE = "Erro simulado";
    private final Exception EXCEPTION = new RuntimeException(EXCEPTION_MESSAGE);

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Captor
    private ArgumentCaptor<Object[]> paramsCaptor;

    private void verificarLogComCategoriaMensagemEParametros(String level, String expectedMessageFormat, Object... expectedParams) {
        switch (level.toLowerCase()) {
            case "info":
                verify(log, times(1)).info(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "debug":
                verify(log, times(1)).debug(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "warn":
                verify(log, times(1)).warn(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "error":
                verify(log, times(1)).error(messageCaptor.capture(), paramsCaptor.capture());
                break;
            default:
                throw new IllegalArgumentException("Nível de log não suportado: " + level);
        }

        assertEquals(expectedMessageFormat, messageCaptor.getValue());
        assertNotNull(paramsCaptor.getValue());
        assertEquals(CATEGORY, paramsCaptor.getValue()[0]);
        for (int i = 0; i < expectedParams.length; i++) {
            assertEquals(expectedParams[i], paramsCaptor.getValue()[i + 1]);
        }
    }

    private void verificarLogComCategoriaEMensagem(String level, String expectedMessageFormat) {
        switch (level.toLowerCase()) {
            case "info":
                verify(log, times(1)).info(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "debug":
                verify(log, times(1)).debug(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "warn":
                verify(log, times(1)).warn(messageCaptor.capture(), paramsCaptor.capture());
                break;
            case "error":
                verify(log, times(1)).error(messageCaptor.capture(), paramsCaptor.capture());
                break;
            default:
                throw new IllegalArgumentException("Nível de log não suportado: " + level);
        }

        assertEquals(expectedMessageFormat, messageCaptor.getValue());
        assertNotNull(paramsCaptor.getValue());
        assertEquals(CATEGORY, paramsCaptor.getValue()[0]);
    }

    private void verificarLogComCategoriaMensagemEExcecao(String level, String expectedMessageFormat, String expectedParam1, Exception expectedParam2) {
        switch (level.toLowerCase()) {
            case "error":
                verify(log, times(1)).error(messageCaptor.capture(), paramsCaptor.capture());
                break;
            default:
                throw new IllegalArgumentException("Nível de log não suportado para exceção: " + level);
        }

        assertEquals(expectedMessageFormat, messageCaptor.getValue());
        assertNotNull(paramsCaptor.getValue());
        assertEquals(CATEGORY, paramsCaptor.getValue()[0]);
        assertEquals(expectedParam1, paramsCaptor.getValue()[1]);
        assertEquals(expectedParam2, paramsCaptor.getValue()[2]);
    }

    private void verificarLogComCategoriaMensagemEUmParametro(String level, String expectedMessageFormat, Object expectedParam1) {
        switch (level.toLowerCase()) {
            case "error":
                verify(log, times(1)).error(messageCaptor.capture(), paramsCaptor.capture());
                break;
            // Adicione outros cases se necessário para outros níveis com um parâmetro
            default:
                throw new IllegalArgumentException("Nível de log não suportado para um parâmetro: " + level);
        }

        assertEquals(expectedMessageFormat, messageCaptor.getValue());
        assertNotNull(paramsCaptor.getValue());
        assertEquals(CATEGORY, paramsCaptor.getValue()[0]);
        assertEquals(expectedParam1, paramsCaptor.getValue()[1]);
    }

    @Test
    void deveLogarMensagemDeInfoComCategoria() {
        assertNotNull(log);
        loggerUtil.logInfo(CATEGORY, MESSAGE);
        verificarLogComCategoriaEMensagem("info", "[{}] Mensagem de teste.");
    }

    @Test
    void deveLogarMensagemDeInfoComCategoriaEParametros() {
        assertNotNull(log);
        loggerUtil.logInfo(CATEGORY, MESSAGE + " - {}", PARAM1, PARAM2);
        verificarLogComCategoriaMensagemEParametros("info", "[{}] Mensagem de teste. - {}", PARAM1, PARAM2);
    }

    @Test
    void deveLogarMensagemDeDebugComCategoria() {
        assertNotNull(log);
        loggerUtil.logDebug(CATEGORY, MESSAGE);
        verificarLogComCategoriaEMensagem("debug", "[{}] Mensagem de teste.");
    }

    @Test
    void deveLogarMensagemDeDebugComCategoriaEParametros() {
        assertNotNull(log);
        loggerUtil.logDebug(CATEGORY, MESSAGE + " - {}", PARAM1, PARAM2);
        verificarLogComCategoriaMensagemEParametros("debug", "[{}] Mensagem de teste. - {}", PARAM1, PARAM2);
    }

    @Test
    void deveLogarMensagemDeAlertaComCategoria() {
        assertNotNull(log);
        loggerUtil.logWarn(CATEGORY, MESSAGE);
        verificarLogComCategoriaEMensagem("warn", "[{}] Mensagem de teste.");
    }

    @Test
    void deveLogarMensagemDeAlertaComCategoriaEParametros() {
        assertNotNull(log);
        loggerUtil.logWarn(CATEGORY, MESSAGE + " - {}", PARAM1, PARAM2);
        verificarLogComCategoriaMensagemEParametros("warn", "[{}] Mensagem de teste. - {}", PARAM1, PARAM2);
    }

    @Test
    void deveLogarMensagemDeErroComCategoria() {
        assertNotNull(log);
        loggerUtil.logError(CATEGORY, MESSAGE);
        verificarLogComCategoriaEMensagem("error", "[{}] Mensagem de teste.");
    }

    @Test
    void deveLogarMensagemDeErroComCategoriaEParametros() {
        assertNotNull(log);
        loggerUtil.logError(CATEGORY, MESSAGE + " - {}", PARAM1, PARAM2);
        verificarLogComCategoriaMensagemEParametros("error", "[{}] Mensagem de teste. - {}", PARAM1, PARAM2);
    }

    @Test
    void deveLogarMensagemDeErroComCategoriaEExcecao() {
        assertNotNull(log);
        loggerUtil.logError(CATEGORY, MESSAGE + " - {}", EXCEPTION_MESSAGE, EXCEPTION);
        verificarLogComCategoriaMensagemEExcecao("error", "[{}] Mensagem de teste. - {}", EXCEPTION_MESSAGE, EXCEPTION);
    }

    @Test
    void deveLogarMensagemDeErroComCategoriaEUmParametro() {
        assertNotNull(log);
        loggerUtil.logError(CATEGORY, MESSAGE + " - {}", PARAM1);
        verificarLogComCategoriaMensagemEUmParametro("error", "[{}] Mensagem de teste. - {}", PARAM1);
    }
}