# PPay Simplificado

PPay Simplificado é um projeto de estudo de Spring Boot para gerenciar transações financeiras e usuários. Este projeto inclui APIs para criar, listar e buscar transações e usuários.

## Requisitos

- Java 23
- Maven
- MySQL

## Tecnologias Utilizadas

- Spring Boot
- Spring Data JPA
- Spring Web
- Spring DevTools
- MySQL Connector
- Lombok
- MapStruct
- Hibernate Validator
- Jakarta EL
- Spring WebFlux
- SpringDoc OpenAPI
- SLF4J
- Jackson

## Configuração do Ambiente

### Banco de Dados

Crie um banco de dados MySQL e atualize as configurações no arquivo `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/seu_banco_de_dados
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```


## Executando o Projeto

1. Clone o repositório:
    ```bash
    git clone https://github.com/felmanc/ppay-desafio-backend.git
    ```

2. Navegue até o diretório do projeto:
    ```bash
    cd ppay-desafio-backend/code/ppaysimplificado
    ```

3. Compile e execute o projeto usando Maven:
    ```bash
    mvn spring-boot:run
    ```

## Endpoints da API

### Usuários

- **Criar Usuário**
    - **URL:** `/user`
    - **Método:** `POST`
    - **Exemplo de Request:**
      ```json
      {
        "nome": "John Doe",
        "cpf": "12345678900",
        "email": "john@example.com",
        "senha": "password",
        "saldo": 0.0,
        "tipo": "COMMON"
      }
      ```
    - **Exemplo de Response:**
      ```json
      {
        "id": 1,
        "nome": "John Doe",
        "cpf": "12345678900",
        "email": "john@example.com",
        "senha": "password",
        "saldo": 0.0,
        "tipo": "COMMON"
      }
      ```

- **Listar Todos os Usuários**
    - **URL:** `/user`
    - **Método:** `GET`

