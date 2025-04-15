package br.com.felmanc.ppaysimplificado.utils;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggerUtilImpl implements LoggerUtil {

    public void logInfo(String category, String message, Object... params) {
        log.info("[{}] " + message, prependCategory(category, params));
    }

    public void logDebug(String category, String message, Object... params) {
        log.debug("[{}] " + message, prependCategory(category, params));
    }

    public void logWarn(String category, String message, Object... params) {
        log.warn("[{}] " + message, prependCategory(category, params));
    }

    public void logError(String category, String message, Object... params) {
        log.error("[{}] " + message, prependCategory(category, params));
    }

    private Object[] prependCategory(String category, Object[] params) {
        Object[] newParams = new Object[params.length + 1];
        newParams[0] = category;
        System.arraycopy(params, 0, newParams, 1, params.length);
        return newParams;
    }
}
