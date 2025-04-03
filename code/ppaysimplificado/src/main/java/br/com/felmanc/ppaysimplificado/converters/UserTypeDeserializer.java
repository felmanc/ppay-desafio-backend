package br.com.felmanc.ppaysimplificado.converters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

public class UserTypeDeserializer extends JsonDeserializer<UserType> {

    private final LoggerUtil loggerUtil;

    public UserTypeDeserializer(LoggerUtil loggerUtil) {
        this.loggerUtil = loggerUtil;
    }

    @Override
    public UserType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        loggerUtil.logDebug("Deserialização", "Texto obtido para deserialização: {}", text);

        try {
            UserType userType = UserType.fromString(text, loggerUtil);
            loggerUtil.logInfo("Deserialização", "UserType deserializado com sucesso: {}", userType);
            return userType;
        } catch (IllegalArgumentException e) {
            loggerUtil.logError("Erro", "Falha na deserialização do UserType: {}", text, e);
            throw e;
        }
    }
}