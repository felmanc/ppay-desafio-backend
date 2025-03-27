package br.com.felmanc.ppaysimplificado.configs;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.felmanc.ppaysimplificado.services.UserService;

@Configuration
public class TestConfig {

	public static final int SERVER_PORT = 8888;
	
    @Bean
    UserService userService() {
        return Mockito.mock(UserService.class);
    }
}
