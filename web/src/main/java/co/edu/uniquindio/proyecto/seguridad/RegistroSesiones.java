package co.edu.uniquindio.proyecto.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones abiertas de cada usuario, para poder cerrarlas cuando el administrador lo bloquea (si
 * no, seguiría dentro hasta que caducara su sesión). Spring Boot registra este bean como listener
 * de sesiones, así que las sesiones que terminan se quitan solas del registro.
 */
@Component
public class RegistroSesiones implements HttpSessionListener {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroSesiones.class);

    private final Map<String, Set<HttpSession>> sesiones = new ConcurrentHashMap<>();

    /** Se llama al iniciar sesión (después de renovar el identificador de la sesión). */
    public void registrar(int tipo, Long idUsuario, HttpSession sesion) {
        sesiones.computeIfAbsent(clave(tipo, idUsuario), k -> ConcurrentHashMap.newKeySet()).add(sesion);
    }

    /** Cierra todas las sesiones abiertas del usuario y devuelve cuántas eran. */
    public int cerrarSesionesDe(int tipo, Long idUsuario) {
        Set<HttpSession> abiertas = sesiones.remove(clave(tipo, idUsuario));
        if (abiertas == null) {
            return 0;
        }
        int cerradas = 0;
        for (HttpSession sesion : abiertas) {
            try {
                sesion.invalidate();
                cerradas++;
            } catch (IllegalStateException yaCerrada) {
                // La sesión ya había terminado
            }
        }
        if (cerradas > 0) {
            LOG.info("Se cerraron {} sesiones del usuario {}", cerradas, clave(tipo, idUsuario));
        }
        return cerradas;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        HttpSession sesion = evento.getSession();
        sesiones.values().forEach(abiertas -> abiertas.remove(sesion));
        sesiones.values().removeIf(Set::isEmpty);
    }

    private static String clave(int tipo, Long idUsuario) {
        return tipo + ":" + idUsuario;
    }
}
