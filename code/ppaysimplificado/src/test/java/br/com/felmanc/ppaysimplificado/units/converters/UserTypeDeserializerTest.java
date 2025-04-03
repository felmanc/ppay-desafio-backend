package br.com.felmanc.ppaysimplificado.units.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import br.com.felmanc.ppaysimplificado.converters.UserTypeDeserializer;
import br.com.felmanc.ppaysimplificado.enums.UserType;

public class UserTypeDeserializerTest {

	@Test
	public void testUserTypeDeserializerValid() throws Exception {
	    // Mock do JsonParser
	    JsonParser parser = mock(JsonParser.class);
	    when(parser.getText()).thenReturn("common");

	    DeserializationContext context = mock(DeserializationContext.class);

	    // Testando a desserialização
	    UserTypeDeserializer deserializer = new UserTypeDeserializer();
	    UserType result = deserializer.deserialize(parser, context);

	    assertEquals(UserType.COMMON, result);
	}

	@Test
	public void testUserTypeDeserializerValid2() throws Exception {
	    // Mock do JsonParser
	    JsonParser parser = mock(JsonParser.class);
	    when(parser.getText()).thenReturn(UserType.COMMON.toString());

	    DeserializationContext context = mock(DeserializationContext.class);

	    // Testando a desserialização
	    UserTypeDeserializer deserializer = new UserTypeDeserializer();
	    UserType result = deserializer.deserialize(parser, context);

	    assertEquals(UserType.COMMON, result);
	}

	@Test
	public void testUserTypeDeserializerInvalid() throws Exception {
	    JsonParser parser = mock(JsonParser.class);
	    when(parser.getText()).thenReturn("invalid");

	    DeserializationContext context = mock(DeserializationContext.class);

	    UserTypeDeserializer deserializer = new UserTypeDeserializer();

	    // Validando exceção para valores inválidos
	    assertThrows(RuntimeException.class, () -> deserializer.deserialize(parser, context));
	}
}