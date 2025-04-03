package br.com.felmanc.ppaysimplificado.enums;

import br.com.felmanc.ppaysimplificado.exceptions.InvalidUserTypeException;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

public enum UserType {
    COMMON,
    MERCHANT;

    public static UserType fromString(String value, LoggerUtil loggerUtil) {
        try {
            loggerUtil.logInfo("Conversão", "Convertendo {} para upper case.", value);
            return UserType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserTypeException("Invalid UserType: " + value);
        }
    }
}