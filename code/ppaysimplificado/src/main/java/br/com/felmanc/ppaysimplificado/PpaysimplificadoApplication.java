package br.com.felmanc.ppaysimplificado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "br.com.felmanc.ppaysimplificado")
public class PpaysimplificadoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PpaysimplificadoApplication.class, args);
	}

}
