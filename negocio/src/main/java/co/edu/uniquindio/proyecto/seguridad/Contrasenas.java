package co.edu.uniquindio.proyecto.seguridad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.regex.Pattern;

/**
 * Cifrado de contraseñas con BCrypt. Las contraseñas se guardan siempre cifradas (lo garantiza
 * {@link CifradoContrasenaListener}) y el login las compara con {@link #coincide}.
 */
public final class Contrasenas {

    private static final BCryptPasswordEncoder CODIFICADOR = new BCryptPasswordEncoder(10);
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    /**
     * Hash de una contraseña cualquiera. Cuando el correo no existe, el login compara contra este
     * hash para tardar lo mismo que con un correo registrado y no delatar cuáles existen.
     */
    private static final String HASH_FICTICIO = CODIFICADOR.encode("contraseña-ficticia-para-igualar-tiempos");

    private Contrasenas() {
    }

    public static String cifrar(String enClaro) {
        return CODIFICADOR.encode(enClaro);
    }

    /** {@code true} si la contraseña en claro corresponde al hash guardado. */
    public static boolean coincide(String enClaro, String hash) {
        if (enClaro == null || hash == null || !estaCifrada(hash)) {
            return false;
        }
        return CODIFICADOR.matches(enClaro, hash);
    }

    /** Compara contra un hash ficticio y devuelve siempre {@code false} (ver {@link #HASH_FICTICIO}). */
    public static boolean compararFicticio(String enClaro) {
        CODIFICADOR.matches(enClaro == null ? "" : enClaro, HASH_FICTICIO);
        return false;
    }

    /**
     * Si el valor ya es un hash BCrypt. Una contraseña en claro no puede parecerlo: tiene como
     * máximo 30 caracteres y un hash tiene 60.
     */
    public static boolean estaCifrada(String valor) {
        return valor != null && BCRYPT.matcher(valor).matches();
    }
}
