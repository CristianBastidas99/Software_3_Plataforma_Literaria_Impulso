package co.edu.uniquindio.proyecto.servicios;

import java.util.Optional;

/** Flujo de "¿Olvidaste tu contraseña?". */
public interface RecuperacionServicio {

    int PASSWORD_MIN = 8;
    int PASSWORD_MAX = 30;

    /**
     * Si el correo pertenece a una cuenta activa, crea un enlace nuevo (invalida los anteriores) y lo
     * envía por correo. Si no, no hace nada: el llamador muestra el mismo mensaje en ambos casos para
     * no revelar qué correos están registrados.
     */
    void solicitarRecuperacion(String email);

    /**
     * Crea el token sin enviar el correo y lo devuelve en claro (en la base de datos solo queda su
     * hash). Vacío si el correo no pertenece a una cuenta activa.
     */
    Optional<String> crearToken(String email);

    /** Devuelve el correo del enlace si sigue vigente; si no, lanza una excepción con un mensaje para el usuario. */
    String validarToken(String token) throws Exception;

    /** Cambia la contraseña de la cuenta del enlace y lo inutiliza (junto con cualquier otro pendiente). */
    void restablecerContrasena(String token, String nueva, String confirmacion) throws Exception;

    int getMinutosValidez();
}
