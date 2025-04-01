package br.com.felmanc.ppaysimplificado.integrations.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import br.com.felmanc.ppaysimplificado.clients.AuthorizationClient;
import br.com.felmanc.ppaysimplificado.configs.IntegrationTestConfig;
import br.com.felmanc.ppaysimplificado.dtos.TransactionDTO;
import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.enums.TransactionStatus;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.integrations.containers.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    private final static String serverPort = Integer.toString(IntegrationTestConfig.SERVER_PORT);

    @MockitoBean
    private AuthorizationClient authorizationClient;
    
    @Autowired
    ApplicationContext context;

    static {
        System.setProperty("server.port", serverPort);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private RequestSpecification specification;
    private static ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        RestAssured.port = IntegrationTestConfig.SERVER_PORT;
        specification = new RequestSpecBuilder()
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new ParameterNamesModule());
    }

    private void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM transactions");
        jdbcTemplate.execute("DELETE FROM users");
    }
    
    @Test
    @Order(1)
    @DirtiesContext
    public void testTransfer() {
        UserDTO payer = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("200.00"), UserType.COMMON);
        UserDTO payee = new UserDTO(null, "Maria Oliveira", "22233344455", "maria@exemplo.com", "senha123", new BigDecimal("50.00"), UserType.COMMON);

        clearDatabase();

        // Create payer
        Response responsePayer = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payer)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayer.statusCode());
        UserDTO createdPayer = responsePayer.getBody().as(UserDTO.class);
        assertNotNull(createdPayer);
        
        // Create payee
        Response responsePayee = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payee)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayee.statusCode());
        UserDTO createdPayee = responsePayee.getBody().as(UserDTO.class);
        assertNotNull(createdPayee);

        // Simulação de resposta bem-sucedida
        when(authorizationClient.authorizeTransaction()).thenReturn(true);
        
        // Create transaction
        TransactionDTO transactionDTO = new TransactionDTO(null, createdPayer.id(), createdPayee.id(), new BigDecimal("100.00"), TransactionStatus.PENDING.name(), null);

        log.info("Enviando requisição para realizar transferência: {}", transactionDTO);
        
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(transactionDTO)
                .when()
                .post("/transfer")
                .then()
                .extract()
                .response();
        
        log.info("Resposta da transferência: {}", response.asString());

        assertEquals(HttpStatus.OK.value(), response.statusCode());

        TransactionDTO createdTransaction = response.getBody().as(TransactionDTO.class);
        assertNotNull(createdTransaction, "A resposta não deve ser null");
        assertEquals(TransactionStatus.COMPLETED.name(), createdTransaction.status());
        assertEquals(new BigDecimal("100.00"), createdTransaction.valor());
    }

    @Test
    @Order(2)
    @DirtiesContext
    public void testGetAllTransactions() {
        UserDTO payer = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("200.00"), UserType.COMMON);
        UserDTO payee = new UserDTO(null, "Maria Oliveira", "22233344455", "maria@exemplo.com", "senha123", new BigDecimal("50.00"), UserType.COMMON);

        clearDatabase();

        // Create payer
        Response responsePayer = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payer)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayer.statusCode());
        UserDTO createdPayer = responsePayer.getBody().as(UserDTO.class);
        assertNotNull(createdPayer);
        
        // Create payee
        Response responsePayee = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payee)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayee.statusCode());
        UserDTO createdPayee = responsePayee.getBody().as(UserDTO.class);
        assertNotNull(createdPayee);

		// Simulação de resposta bem-sucedida para autorização
		when(authorizationClient.authorizeTransaction()).thenReturn(true);

        // Create transaction
        TransactionDTO transactionDTO = new TransactionDTO(null, createdPayer.id(), createdPayee.id(), new BigDecimal("100.00"), TransactionStatus.PENDING.name(), null);

        log.info("Enviando requisição para realizar transferência: {}", transactionDTO);
        
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(transactionDTO)
                .when()
                .post("/transfer")
                .then()
                .extract()
                .response();
        
        log.info("Resposta da transferência: {}", response.asString());
        assertEquals(HttpStatus.OK.value(), response.statusCode());

        // Get all transactions
        log.info("Enviando requisição para buscar todas as transações");

        Response getAllResponse = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/transfer")
                .then()
                .extract()
                .response();
        
        log.info("Resposta da busca de todas as transações: {}", getAllResponse.asString());

        assertEquals(HttpStatus.OK.value(), getAllResponse.statusCode());

        List<TransactionDTO> transactions = getAllResponse.jsonPath().getList(".", TransactionDTO.class);
        assertNotNull(transactions, "A lista de transações não deve ser null");
        assertTrue(transactions.size() > 0, "Deve haver pelo menos 1 transação no banco");
    }
    
    @Test
    @Order(3)
    @DirtiesContext
    public void testTransferUnauthorized() {
        UserDTO payer = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("200.00"), UserType.COMMON);
        UserDTO payee = new UserDTO(null, "Maria Oliveira", "22233344455", "maria@exemplo.com", "senha123", new BigDecimal("50.00"), UserType.COMMON);

        clearDatabase();

        // Create payer
        Response responsePayer = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payer)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayer.statusCode());
        UserDTO createdPayer = responsePayer.getBody().as(UserDTO.class);
        assertNotNull(createdPayer);

        // Create payee
        Response responsePayee = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(payee)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
        assertEquals(HttpStatus.OK.value(), responsePayee.statusCode());
        UserDTO createdPayee = responsePayee.getBody().as(UserDTO.class);
        assertNotNull(createdPayee);

        when(authorizationClient.authorizeTransaction()).thenReturn(false);
        
        // Simulate unauthorized transaction
        TransactionDTO transactionDTO = new TransactionDTO(null, createdPayer.id(), createdPayee.id(), new BigDecimal("100.00"), TransactionStatus.PENDING.name(), null);

        log.info("Enviando requisição para realizar transferência não autorizada: {}", transactionDTO);
                
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(transactionDTO)
                .when()
                .post("/transfer")
                .then()
                .extract()
                .response();
        
        log.info("Resposta da transferência não autorizada: {}", response.asString());

        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());

        // Verificações de saldo após a exceção (reversão do @Transactional)
        UserDTO updatedPayer = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/user/" + createdPayer.id())
                .then()
                .extract()
                .response()
                .getBody()
                .as(UserDTO.class);
        UserDTO updatedPayee = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/user/" + createdPayee.id())
                .then()
                .extract()
                .response()
                .getBody()
                .as(UserDTO.class);
        assertEquals(new BigDecimal("200.00"), updatedPayer.saldo());
        assertEquals(new BigDecimal("50.00"), updatedPayee.saldo());
    }
}