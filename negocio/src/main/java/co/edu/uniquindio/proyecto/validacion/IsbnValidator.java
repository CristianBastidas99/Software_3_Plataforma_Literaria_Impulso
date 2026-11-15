package co.edu.uniquindio.proyecto.validacion;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Comprueba el dígito de control de un ISBN-10 (módulo 11, la última posición puede ser X)
 * o de un ISBN-13 (pesos 1 y 3, módulo 10).
 */
public class IsbnValidator implements ConstraintValidator<Isbn, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return esValido(valor);
    }

    public static boolean esValido(String valor) {
        String isbn = normalizar(valor);
        if (isbn.length() == 13) {
            return esIsbn13(isbn);
        }
        if (isbn.length() == 10) {
            return esIsbn10(isbn);
        }
        return false;
    }

    /** Quita guiones y espacios y pasa la X final a mayúscula. */
    public static String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("[\\s-]", "").toUpperCase();
    }

    private static boolean esIsbn13(String isbn) {
        if (!isbn.matches("\\d{13}")) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 13; i++) {
            int digito = isbn.charAt(i) - '0';
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        return suma % 10 == 0;
    }

    private static boolean esIsbn10(String isbn) {
        if (!isbn.matches("\\d{9}[\\dX]")) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int digito = c == 'X' ? 10 : c - '0';
            suma += (10 - i) * digito;
        }
        return suma % 11 == 0;
    }
}
