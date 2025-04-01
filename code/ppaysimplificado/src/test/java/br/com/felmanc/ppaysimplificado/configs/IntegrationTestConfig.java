package br.com.felmanc.ppaysimplificado.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("integration-test")
public class IntegrationTestConfig {

	public static final int SERVER_PORT = 8888;
}
