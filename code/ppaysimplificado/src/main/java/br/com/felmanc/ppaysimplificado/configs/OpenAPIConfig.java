package br.com.felmanc.ppaysimplificado.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenAPIConfig {
	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Desafio Back-end PPay")
						.version("v1")
						.description("Desenvolvimento de uma plataforma de pagamentos simplificada."));
	}
}
