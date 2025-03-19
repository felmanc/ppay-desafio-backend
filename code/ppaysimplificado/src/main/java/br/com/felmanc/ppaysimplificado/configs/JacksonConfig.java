package br.com.felmanc.ppaysimplificado.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import br.com.felmanc.ppaysimplificado.converters.UserTypeDeserializer;
import br.com.felmanc.ppaysimplificado.enums.UserType;

@Configuration
public class JacksonConfig {

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addDeserializer(UserType.class, new UserTypeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }
}
