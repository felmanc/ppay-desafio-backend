package br.com.felmanc.ppaysimplificado.units.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import br.com.felmanc.ppaysimplificado.converters.UserTypeDeserializer;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;

public class UserTypeTest {

	@Mock
	private LoggerUtil loggerUtil;

	@BeforeEach
	void setUp() {
	    loggerUtil = mock(LoggerUtil.class); // Inicializa o mock de LoggerUtil
	}

    @Test
    public void testUserTypeFromString() {
        assertEquals(UserType.COMMON, UserType.fromString("common"));
        assertEquals(UserType.MERCHANT, UserType.fromString("merchant"));
        assertThrows(RuntimeException.class, () -> UserType.fromString("invalid"));
    }

    @Test
    public void testUserTypeDeserializerValid() throws Exception {
        // Mock do JsonParser
        JsonParser parser = mock(JsonParser.class);
        when(parser.getText()).thenReturn("common");

        DeserializationContext context = mock(DeserializationContext.class);

        // Testando a desserialização
        UserTypeDeserializer deserializer = new UserTypeDeserializer(loggerUtil);
        UserType result = deserializer.deserialize(parser, context);

        assertEquals(UserType.COMMON, result);
    }

    @Test
    public void testUserTypeDeserializerInvalid() throws Exception {
        JsonParser parser = mock(JsonParser.class);
        when(parser.getText()).thenReturn("invalid");

        DeserializationContext context = mock(DeserializationContext.class);

        UserTypeDeserializer deserializer = new UserTypeDeserializer(loggerUtil);

        // Validando exceção para valores inválidos
        assertThrows(RuntimeException.class, () -> deserializer.deserialize(parser, context));
    }
}
