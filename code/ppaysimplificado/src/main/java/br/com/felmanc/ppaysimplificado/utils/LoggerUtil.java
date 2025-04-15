package br.com.felmanc.ppaysimplificado.utils;

public interface LoggerUtil {

    void logInfo(String category, String message, Object... params);

    void logDebug(String category, String message, Object... params);

    void logWarn(String category, String message, Object... params);

    void logError(String category, String message, Object... params);
}
