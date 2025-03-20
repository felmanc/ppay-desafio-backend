package br.com.felmanc.ppaysimplificado.configs;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.felmanc.ppaysimplificado.services.UserService;

@Configuration
public class TestConfig {

    @Bean
    UserService userService() {
        return Mockito.mock(UserService.class);
    }
}
