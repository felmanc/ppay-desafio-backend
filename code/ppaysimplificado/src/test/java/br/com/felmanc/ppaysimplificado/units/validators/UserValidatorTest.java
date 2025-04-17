package br.com.felmanc.ppaysimplificado.units.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.felmanc.ppaysimplificado.entities.UserEntity;
import br.com.felmanc.ppaysimplificado.enums.UserType;
import br.com.felmanc.ppaysimplificado.validators.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;

    private UserEntity createUserEntity(BigDecimal balance) {
        UserEntity user = new UserEntity();
        user.setBalance(balance);
        return user;
    }

    @Test
    void testValidarSaldoSuficiente_Success() {
        UserEntity user = createUserEntity(new BigDecimal("100.00"));
        BigDecimal valor = new BigDecimal("50.00");
        assertDoesNotThrow(() -> userValidator.validarSaldoSuficiente(user, valor));
    }

    @Test
    void testValidarSaldoSuficiente_InsufficientBalance() {
        UserEntity user = createUserEntity(new BigDecimal("50.00"));
        BigDecimal valor = new BigDecimal("100.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarSaldoSuficiente(user, valor));
        assertEquals("Saldo insuficiente.", exception.getMessage());
    }

    @Test
    void testValidarSaldoSuficiente_InvalidValueZero() {
        UserEntity user = createUserEntity(new BigDecimal("100.00"));
        BigDecimal valor = BigDecimal.ZERO;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarSaldoSuficiente(user, valor));
        assertEquals("O valor do débito deve ser positivo.", exception.getMessage());
    }

    @Test
    void testValidarSaldoSuficiente_InvalidValueNegative() {
        UserEntity user = createUserEntity(new BigDecimal("100.00"));
        BigDecimal valor = new BigDecimal("-10.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarSaldoSuficiente(user, valor));
        assertEquals("O valor do débito deve ser positivo.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_Success() {
        assertDoesNotThrow(() -> userValidator.validarDadosUsuario("Nome Teste", "12345678900", "teste@example.com", "senha123", UserType.COMMON));
        assertDoesNotThrow(() -> userValidator.validarDadosUsuario("Empresa Teste", "12345678901234", "teste@example.com", "senha123", UserType.MERCHANT));
    }

    @Test
    void testValidarDadosUsuario_NomeObrigatorio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario(null, "12345678900", "teste@example.com", "senha123", UserType.COMMON));
        assertEquals("O nome do usuário é obrigatório.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_CpfObrigatorio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", null, "teste@example.com", "senha123", UserType.COMMON));
        assertEquals("O CPF/CNPJ é obrigatório.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_CpfFormatoInvalidoCurto() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "123", "teste@example.com", "senha123", UserType.COMMON));
        assertEquals("O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_CpfFormatoInvalidoNaoNumerico() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "123a5678900", "teste@example.com", "senha123", UserType.COMMON));
        assertEquals("O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_EmailObrigatorio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "12345678900", null, "senha123", UserType.COMMON));
        assertEquals("O e-mail é obrigatório.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_EmailFormatoInvalidoSemArroba() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "12345678900", "testeexample.com", "senha123", UserType.COMMON));
        assertEquals("O e-mail deve estar em um formato válido.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_SenhaObrigatoria() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "12345678900", "teste@example.com", null, UserType.COMMON));
        assertEquals("A senha é obrigatória.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_TipoObrigatorio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Nome Teste", "12345678900", "teste@example.com", "senha123", null));
        assertEquals("O tipo do usuário (COMMON ou MERCHANT) é obrigatório.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_CnpjFormatoInvalidoCurto() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Empresa Teste", "1234567890123", "teste@example.com", "senha123", UserType.MERCHANT));
        assertEquals("O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.", exception.getMessage());
    }

    @Test
    void testValidarDadosUsuario_CnpjFormatoInvalidoNaoNumerico() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validarDadosUsuario("Empresa Teste", "1234567890123a", "teste@example.com", "senha123", UserType.MERCHANT));
        assertEquals("O CPF deve conter 11 dígitos ou o CNPJ deve conter 14 dígitos e somente números.", exception.getMessage());
    }
}