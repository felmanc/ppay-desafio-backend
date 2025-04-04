package br.com.felmanc.ppaysimplificado.integrations.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import br.com.felmanc.ppaysimplificado.configs.IntegrationTestConfig;
import br.com.felmanc.ppaysimplificado.dtos.UserDTO;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.integrations.containers.AbstractIntegrationTest;
import br.com.felmanc.ppaysimplificado.utils.LoggerUtil;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private final static String serverPort = Integer.toString(IntegrationTestConfig.SERVER_PORT);

    @Autowired
    ApplicationContext context;

    @Autowired
    private LoggerUtil loggerUtil;

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
    public void testCreateUser() {
        UserDTO userDTO = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("1000.00"), UserType.COMMON);
    
        clearDatabase();
        
        loggerUtil.logInfo("Teste", "Enviando requisição para criar usuário: {}", userDTO);
        
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
    
        loggerUtil.logInfo("Teste", "Resposta da criação de usuário: {}", response.asString());
    
        if (response.statusCode() != HttpStatus.OK.value()) {
            loggerUtil.logInfo("Teste", "Erro ao criar usuário: {}", response.asString());
        }
        
        assertEquals(HttpStatus.OK.value(), response.statusCode());
    
        UserDTO createdUser = null;
        try {
            createdUser = objectMapper.readValue(response.asString(), UserDTO.class);
        } catch (JsonMappingException e) {
            loggerUtil.logError("Teste", "Erro ao mapear JSON para UserDTO", e);
        } catch (JsonProcessingException e) {
            loggerUtil.logError("Teste", "Erro ao processar JSON", e);
        }
    
        assertNotNull(createdUser, "A resposta não deve ser null");
        loggerUtil.logInfo("Teste", "Usuário criado: {}", createdUser);
        assertNotNull(createdUser.id(), "O ID do usuário não deve ser null");
        assertEquals(userDTO.nome(), createdUser.nome(), "O nome do usuário deve ser igual ao esperado");
    }
    
    @Test
    @Order(2)
    public void testCreateUserFail() {
        UserDTO userDTO = new UserDTO(1L, "João da Silva", "111.222.333-44", "joao@literatura.com.br", "senha123", new BigDecimal("1000.00"), UserType.COMMON);
    
        loggerUtil.logInfo("Teste", "Enviando requisição para criar usuário (falha esperada): {}", userDTO);
        
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();
    
        loggerUtil.logInfo("Teste", "Resposta da criação de usuário (falha): {}", response.asString());
    
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
    }
    
    @Test
    @Order(3)
    @DirtiesContext
    public void testGetAllUsers_NoUsersFound() {
        clearDatabase();
        
        loggerUtil.logInfo("Teste", "Enviando requisição para buscar todos os usuários");

        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/user")
                .then()
                .extract()
                .response();

        loggerUtil.logInfo("Teste", "Resposta da busca de todos os usuários: {}", response.asString());

        if (response.statusCode() != HttpStatus.NO_CONTENT.value()) {
            loggerUtil.logInfo("Teste", "Erro ao buscar todos os usuários: {}", response.asString());
        }

        assertEquals(HttpStatus.NO_CONTENT.value(), response.statusCode());
    }

    @Test
    @Order(4)
    @DirtiesContext
    public void testGetAllUsers_UsersFound() {
        UserDTO userDTO = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("1000.00"), UserType.COMMON);

        clearDatabase();
        
        loggerUtil.logInfo("Teste", "Enviando requisição para criar usuário: {}", userDTO);

        Response createUserResponse = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();

        loggerUtil.logInfo("Teste", "Resposta da criação de usuário: {}", createUserResponse.asString());

        assertEquals(HttpStatus.OK.value(), createUserResponse.statusCode());

        UserDTO createdUser = createUserResponse.getBody().as(UserDTO.class);
        assertNotNull(createdUser, "A resposta não deve ser null");
        assertNotNull(createdUser.id(), "O ID do usuário não deve ser null");
        assertEquals(userDTO.nome(), createdUser.nome(), "O nome do usuário deve ser igual ao esperado");

        // Buscar todos os usuários
        loggerUtil.logInfo("Teste", "Enviando requisição para buscar todos os usuários");

        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/user")
                .then()
                .extract()
                .response();

        loggerUtil.logInfo("Teste", "Resposta da busca de todos os usuários: {}", response.asString());

        if (response.statusCode() != HttpStatus.OK.value()) {
            loggerUtil.logInfo("Teste", "Erro ao buscar todos os usuários: {}", response.asString());
        }

        assertEquals(HttpStatus.OK.value(), response.statusCode());

        UserDTO[] users = response.getBody().as(UserDTO[].class);
        assertNotNull(users, "A lista de usuários não deve ser null");
        assertTrue(users.length > 0, "Deve haver pelo menos 1 usuário no banco");
    }
    
    @Test
    @Order(5)
    public void testGetUserById() {
        UserDTO userDTO = new UserDTO(null, "João da Silva", "11122233344", "joao@literatura.com.br", "senha123", new BigDecimal("1000.00"), UserType.COMMON);

        clearDatabase();
        
        loggerUtil.logInfo("Teste", "Enviando requisição para criar usuário: {}", userDTO);

        Response createUserResponse = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .body(userDTO)
                .when()
                .post("/user")
                .then()
                .extract()
                .response();

        loggerUtil.logInfo("Teste", "Resposta da criação de usuário: {}", createUserResponse.asString());

        assertEquals(HttpStatus.OK.value(), createUserResponse.statusCode());

        UserDTO createdUser = createUserResponse.getBody().as(UserDTO.class);
        assertNotNull(createdUser, "A resposta não deve ser null");
        assertNotNull(createdUser.id(), "O ID do usuário não deve ser null");
        assertEquals(userDTO.nome(), createdUser.nome(), "O nome do usuário deve ser igual ao esperado");
        
        Long userId = createdUser.id();

        loggerUtil.logInfo("Teste", "Enviando requisição para buscar usuário por ID: {}", userId);
    
        Response response = given()
                .spec(specification)
                .contentType(ContentType.JSON)
                .when()
                .get("/user/" + userId)
                .then()
                .extract()
                .response();

        loggerUtil.logInfo("Teste", "Resposta da busca de usuário por ID: {}", response.asString());

        if (response.statusCode() != HttpStatus.OK.value()) {
            loggerUtil.logInfo("Teste", "Erro ao buscar usuário por ID: {}", response.asString());
        }

        assertEquals(HttpStatus.OK.value(), response.statusCode());

        UserDTO user = response.getBody().as(UserDTO.class);
        assertNotNull(user, "O usuário não deve ser null");
        assertEquals(userId, user.id());
    }
}