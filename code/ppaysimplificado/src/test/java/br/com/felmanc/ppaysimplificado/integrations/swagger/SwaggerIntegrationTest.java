package br.com.felmanc.ppaysimplificado.integrations.swagger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.felmanc.ppaysimplificado.configs.IntegrationTestConfig;
import br.com.felmanc.ppaysimplificado.integrations.containers.AbstractIntegrationTest;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SwaggerIntegrationTest extends AbstractIntegrationTest{

	private final static String serverPort = Integer.toString(IntegrationTestConfig.SERVER_PORT);
	
    static {
        System.setProperty("server.port", serverPort);
    }
	
	@Test
	public void shouldDisplaySwaggerUiPage() {
		var content = 
			given()
				.basePath("/swagger-ui/index.html")
				.port(IntegrationTestConfig.SERVER_PORT)
				.when()
					.get()
				.then()
					.statusCode(200)
				.extract()
					.body().asString();

		assertTrue(content.contains("Swagger UI"));
		
	}

	@Test
	public void shouldValidateSwaggerContractComplete() {
	    var openApiContent =
	        given()
	            .basePath("/v3/api-docs")
	            .port(IntegrationTestConfig.SERVER_PORT)
	            .when()
	                .get()
	            .then()
	                .statusCode(200)
	            .extract()
	                .body().asString();

	    // Validando OpenAPI e informações principais
	    assertTrue(openApiContent.contains("\"openapi\":\"3.1.0\""), "OpenAPI version is incorrect");
	    assertTrue(openApiContent.contains("\"title\":\"Desafio Back-end PPay\""), "API title is missing");
	    assertTrue(openApiContent.contains("\"description\":\"Desenvolvimento de uma plataforma de pagamentos simplificada.\""), "API description is missing");
	    assertTrue(openApiContent.contains("\"url\":\"http://localhost:" + serverPort + "\""), "Server URL is incorrect");

	    // Validando os endpoints
	    assertTrue(openApiContent.contains("\"/user\""), "Endpoint '/user' is missing");
	    assertTrue(openApiContent.contains("\"/transfer\""), "Endpoint '/transfer' is missing");
	    assertTrue(openApiContent.contains("\"/user/{id}\""), "Endpoint '/user/{id}' is missing");

	    // Validando descrições e tags
	    assertTrue(openApiContent.contains("\"tags\":[{\"name\":\"Transaction Controller\",\"description\":\"APIs relacionadas a operações de transação\"}," +
	        "{\"name\":\"User Controller\",\"description\":\"APIs relacionadas a operações de usuário\"}]"), "Tags description is incorrect");

	    // Validando schemas e objetos
	    assertTrue(openApiContent.contains("\"UserDTO\":{\"type\":\"object\""), "UserDTO schema is missing");
	    assertTrue(openApiContent.contains("\"TransactionDTO\":{\"type\":\"object\""), "TransactionDTO schema is missing");

	    // Validando operações específicas
	    assertTrue(openApiContent.contains("\"operationId\":\"getAllUsers\""), "Operation 'getAllUsers' is missing");
	    assertTrue(openApiContent.contains("\"operationId\":\"createUser\""), "Operation 'createUser' is missing");
	    assertTrue(openApiContent.contains("\"operationId\":\"getUserById\""), "Operation 'getUserById' is missing");
	    assertTrue(openApiContent.contains("\"operationId\":\"getAllTransactions\""), "Operation 'getAllTransactions' is missing");
	    assertTrue(openApiContent.contains("\"operationId\":\"transfer\""), "Operation 'transfer' is missing");
	}
}
