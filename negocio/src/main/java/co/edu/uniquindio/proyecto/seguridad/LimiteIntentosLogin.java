package co.edu.uniquindio.proyecto.seguridad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena los ataques de fuerza bruta contra el login: tras {@link #MAX_FALLOS} contraseñas incorrectas
 * para el mismo correo dentro de {@link #VENTANA}, ese correo no puede iniciar sesión durante
 * {@link #BLOQUEO}. Un login correcto reinicia la cuenta.
 *
 * <p>Se guarda en memoria: se pierde al reiniciar la aplicación y no se comparte entre instancias.
 * Se cuenta por correo y no por IP para no depender de la capa web; la contrapartida es que alguien
 * podría bloquear temporalmente el login de otra persona, por eso el bloqueo es corto.</p>
 */
@Component
public class LimiteIntentosLogin {

    public static final int MAX_FALLOS = 5;
    public static final Duration VENTANA = Duration.ofMinutes(15);
    public static final Duration BLOQUEO = Duration.ofMinutes(15);
    /** A partir de este tamaño se descartan las entradas caducadas, para que el mapa no crezca sin límite. */
    private static final int LIMPIAR_A_PARTIR_DE = 10_000;

    private final Clock reloj;
    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();

    @Autowired
    public LimiteIntentosLogin() {
        this(Clock.systemUTC());
    }

    public LimiteIntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    /** Si el correo está bloqueado, el tiempo que falta (redondeado a minutos hacia arriba); si no, 0. */
    public long minutosBloqueado(String email) {
        String clave = clave(email);
        if (clave == null) {
            return 0;
        }
        Intentos i = intentos.get(clave);
        if (i == null || i.bloqueadoHasta == null) {
            return 0;
        }
        Duration resto = Duration.between(reloj.instant(), i.bloqueadoHasta);
        if (resto.isNegative() || resto.isZero()) {
            return 0;
        }
        return (resto.getSeconds() + 59) / 60;
    }

    public void registrarFallo(String email) {
        String clave = clave(email);
        if (clave == null) {
            return;
        }
        Instant ahora = reloj.instant();
        if (intentos.size() >= LIMPIAR_A_PARTIR_DE) {
            intentos.values().removeIf(i -> i.caducado(ahora));
        }
        intentos.compute(clave, (k, i) -> {
            if (i == null || i.caducado(ahora)) {
                i = new Intentos(ahora);
            }
            i.fallos++;
            if (i.fallos >= MAX_FALLOS) {
                i.bloqueadoHasta = ahora.plus(BLOQUEO);
                i.fallos = 0;
                i.primerFallo = ahora;
            }
            return i;
        });
    }

    public void reiniciar(String email) {
        String clave = clave(email);
        if (clave != null) {
            intentos.remove(clave);
        }
    }

    private static String clave(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    private static final class Intentos {
        private Instant primerFallo;
        private int fallos;
        private Instant bloqueadoHasta;

        private Intentos(Instant primerFallo) {
            this.primerFallo = primerFallo;
        }

        /** Ni hay bloqueo vigente ni fallos recientes que contar. */
        private boolean caducado(Instant ahora) {
            boolean bloqueoVigente = bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
            return !bloqueoVigente && primerFallo.plus(VENTANA).isBefore(ahora);
        }
    }
}
