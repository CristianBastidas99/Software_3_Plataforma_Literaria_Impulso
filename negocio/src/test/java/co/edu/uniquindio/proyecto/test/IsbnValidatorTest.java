package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.validacion.IsbnValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IsbnValidatorTest {

    @Test
    public void isbn13() {
        assertTrue(IsbnValidator.esValido("978-958-99-0070-3"));
        assertTrue(IsbnValidator.esValido("9789589900703"));
        assertTrue(IsbnValidator.esValido("978 0 306 40615 7"));
        assertFalse(IsbnValidator.esValido("978-958-99-0070-4"));
        assertFalse(IsbnValidator.esValido("97895899007A3"));
    }

    @Test
    public void isbn10() {
        assertTrue(IsbnValidator.esValido("0-306-40615-2"));
        assertTrue(IsbnValidator.esValido("0-8044-2957-X"));
        assertTrue(IsbnValidator.esValido("080442957x"));
        assertFalse(IsbnValidator.esValido("1234567890"));
        assertFalse(IsbnValidator.esValido("X804429570"));
    }

    @Test
    public void longitudIncorrecta() {
        assertFalse(IsbnValidator.esValido(""));
        assertFalse(IsbnValidator.esValido("12345"));
        assertFalse(IsbnValidator.esValido("978-958-99-0070-33"));
        assertTrue(new IsbnValidator().isValid(null, null));
    }
}
