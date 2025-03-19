package br.com.felmanc.ppaysimplificado.converters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import br.com.felmanc.ppaysimplificado.enums.UserType;

public class UserTypeDeserializer extends JsonDeserializer<UserType> {
    @Override
    public UserType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return UserType.fromString(p.getText());
    }
}