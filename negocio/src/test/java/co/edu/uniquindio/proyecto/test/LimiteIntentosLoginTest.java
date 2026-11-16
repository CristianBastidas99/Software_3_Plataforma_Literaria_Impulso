package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.seguridad.LimiteIntentosLogin;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Límite de intentos de login, con un reloj que se adelanta a mano. */
public class LimiteIntentosLoginTest {

    /** Reloj de prueba que solo avanza cuando se le pide. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-11-15T10:00:00Z");

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final RelojManual reloj = new RelojManual();
    private final LimiteIntentosLogin limite = new LimiteIntentosLogin(reloj);

    private void fallar(String email, int veces) {
        for (int i = 0; i < veces; i++) {
            limite.registrarFallo(email);
        }
    }

    @Test
    public void bloqueaTrasCincoFallosYSeLiberaAlPasarElTiempo() {
        fallar("ana@example.com", LimiteIntentosLogin.MAX_FALLOS - 1);
        assertEquals(0, limite.minutosBloqueado("ana@example.com"));

        limite.registrarFallo("ana@example.com");
        assertEquals(15, limite.minutosBloqueado("ana@example.com"));
        // Mayúsculas y espacios cuentan como el mismo correo; otro correo no se ve afectado
        assertEquals(15, limite.minutosBloqueado("  ANA@example.com "));
        assertEquals(0, limite.minutosBloqueado("otra@example.com"));

        reloj.avanzar(Duration.ofMinutes(14).plusSeconds(1));
        assertEquals(1, limite.minutosBloqueado("ana@example.com"));
        reloj.avanzar(Duration.ofMinutes(1));
        assertEquals(0, limite.minutosBloqueado("ana@example.com"));

        // Tras el bloqueo se vuelve a empezar de cero
        fallar("ana@example.com", LimiteIntentosLogin.MAX_FALLOS - 1);
        assertEquals(0, limite.minutosBloqueado("ana@example.com"));
    }

    @Test
    public void unLoginCorrectoReiniciaLaCuenta() {
        fallar("luis@example.com", LimiteIntentosLogin.MAX_FALLOS - 1);
        limite.reiniciar("luis@example.com");
        fallar("luis@example.com", LimiteIntentosLogin.MAX_FALLOS - 1);
        assertEquals(0, limite.minutosBloqueado("luis@example.com"));
    }

    @Test
    public void losFallosAntiguosNoCuentan() {
        fallar("eva@example.com", LimiteIntentosLogin.MAX_FALLOS - 1);
        reloj.avanzar(LimiteIntentosLogin.VENTANA.plusSeconds(1));
        limite.registrarFallo("eva@example.com");
        assertEquals(0, limite.minutosBloqueado("eva@example.com"));
    }

    @Test
    public void ignoraCorreosVacios() {
        fallar(null, 10);
        fallar("  ", 10);
        assertEquals(0, limite.minutosBloqueado(null));
        assertEquals(0, limite.minutosBloqueado(" "));
    }
}
