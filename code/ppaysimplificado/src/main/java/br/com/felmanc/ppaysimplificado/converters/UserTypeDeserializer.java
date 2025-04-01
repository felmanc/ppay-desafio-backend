package br.com.felmanc.ppaysimplificado.converters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import br.com.felmanc.ppaysimplificado.enums.UserType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserTypeDeserializer extends JsonDeserializer<UserType> {

    @Override
    public UserType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        log.debug("[Deserialização] Texto obtido para deserialização: {}", text);

        try {
            UserType userType = UserType.fromString(text);
            log.info("[Deserialização] UserType deserializado com sucesso: {}", userType);
            return userType;
        } catch (IllegalArgumentException e) {
            log.error("[Erro] Falha na deserialização do UserType: {}", text, e);
            throw e;
        }
    }
}
