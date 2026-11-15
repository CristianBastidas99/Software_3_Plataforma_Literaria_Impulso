package co.edu.uniquindio.proyecto.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionUsuarioTest {

    @Test
    void nombre() {
        assertNull(ValidacionUsuario.validarNombre("Valentina Ríos"));
        assertNotNull(ValidacionUsuario.validarNombre(null));
        assertNotNull(ValidacionUsuario.validarNombre("   "));
        assertNull(ValidacionUsuario.validarNombre("a".repeat(30)));
        assertNotNull(ValidacionUsuario.validarNombre("a".repeat(31)));
    }

    @Test
    void email() {
        assertNull(ValidacionUsuario.validarEmail("lector@impulso.com"));
        assertNull(ValidacionUsuario.validarEmail("  lector@impulso.com  "));
        assertNotNull(ValidacionUsuario.validarEmail(""));
        assertNotNull(ValidacionUsuario.validarEmail("sin-arroba.com"));
        assertNotNull(ValidacionUsuario.validarEmail("dos@@arrobas.com"));
        assertNotNull(ValidacionUsuario.validarEmail("sin@dominio"));
        assertNotNull(ValidacionUsuario.validarEmail("con espacio@impulso.com"));
        assertNotNull(ValidacionUsuario.validarEmail("a".repeat(40) + "@impulso.com"));
    }

    @Test
    void password() {
        assertNull(ValidacionUsuario.validarPassword("clave123"));
        assertNotNull(ValidacionUsuario.validarPassword(null));
        assertNotNull(ValidacionUsuario.validarPassword(""));
        assertTrue(ValidacionUsuario.validarPassword("corta").contains("al menos 8"));
        assertNull(ValidacionUsuario.validarPassword("x".repeat(30)));
        assertNotNull(ValidacionUsuario.validarPassword("x".repeat(31)));
    }

    @Test
    void biografia() {
        assertNull(ValidacionUsuario.validarBiografia(null));
        assertNull(ValidacionUsuario.validarBiografia(""));
        assertNull(ValidacionUsuario.validarBiografia("b".repeat(250)));
        assertNotNull(ValidacionUsuario.validarBiografia("b".repeat(251)));
    }

    @Test
    void normalizaEmail() {
        assertEquals("lector@impulso.com", ValidacionUsuario.normalizarEmail("  Lector@Impulso.COM "));
        assertNull(ValidacionUsuario.normalizarEmail(null));
    }
}
