package co.edu.uniquindio.proyecto.util;

import java.util.regex.Pattern;

/**
 * Reglas de validación de los datos de un usuario, compartidas por el registro y la edición de perfil.
 * Los límites coinciden con las columnas de la base de datos ({@code Usuario} y {@code Escritor}).
 *
 * <p>Cada método devuelve el mensaje de error para mostrar al usuario, o {@code null} si el valor es válido.</p>
 */
public final class ValidacionUsuario {

    public static final int NOMBRE_MAX = 30;
    public static final int EMAIL_MAX = 50;
    public static final int BIOGRAFIA_MAX = 250;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 30;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidacionUsuario() {
    }

    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre no puede estar vacío";
        }
        if (nombre.trim().length() > NOMBRE_MAX) {
            return "El nombre no puede tener más de " + NOMBRE_MAX + " caracteres";
        }
        return null;
    }

    public static String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            return "El correo electrónico no puede estar vacío";
        }
        String limpio = email.trim();
        if (limpio.length() > EMAIL_MAX) {
            return "El correo electrónico no puede tener más de " + EMAIL_MAX + " caracteres";
        }
        if (!EMAIL.matcher(limpio).matches()) {
            return "El correo electrónico no tiene un formato válido";
        }
        return null;
    }

    public static String validarBiografia(String biografia) {
        if (biografia != null && biografia.trim().length() > BIOGRAFIA_MAX) {
            return "La biografía no puede tener más de " + BIOGRAFIA_MAX + " caracteres";
        }
        return null;
    }

    public static String validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        if (password.length() < PASSWORD_MIN) {
            return "La contraseña debe tener al menos " + PASSWORD_MIN + " caracteres";
        }
        if (password.length() > PASSWORD_MAX) {
            return "La contraseña no puede tener más de " + PASSWORD_MAX + " caracteres";
        }
        return null;
    }

    /** Normaliza un correo para guardarlo y compararlo: sin espacios y en minúsculas. */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
