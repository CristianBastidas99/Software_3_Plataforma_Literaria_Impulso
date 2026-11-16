package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import co.edu.uniquindio.proyecto.seguridad.LimiteIntentosLogin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LoginServicio {

    public static final String CREDENCIALES_INCORRECTAS = "Correo o contraseña incorrectos";
    public static final String DEMASIADOS_INTENTOS = "Demasiados intentos fallidos con este correo. Inténtalo de nuevo en %d %s.";
    public static final String CUENTA_BLOQUEADA = "Tu cuenta está bloqueada. Si crees que es un error, escribe al administrador de la plataforma.";

    private final AdministradorRepo administradorRepo;
    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;
    private final LimiteIntentosLogin limiteIntentos;

    public LoginServicio(AdministradorRepo administradorRepo, EscritorRepo escritorRepo, LectorRepo lectorRepo,
                         LimiteIntentosLogin limiteIntentos) {
        this.administradorRepo = administradorRepo;
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
        this.limiteIntentos = limiteIntentos;
    }

    public boolean isEmailRegistrado(String emailString) {
        return buscarPorCorreo(emailString).isPresent();
    }

    /**
     * Inicia sesión con cualquier tipo de usuario. Devuelve el {@code Administrador}, {@code Escritor}
     * o {@code Lector} correspondiente, o vacío si el correo no existe o la contraseña no coincide
     * (el llamador debe mostrar {@link #CREDENCIALES_INCORRECTAS} en ambos casos).
     *
     * <p>Siempre se hace una comparación BCrypt, también cuando el correo no existe, para que el
     * tiempo de respuesta no delate qué correos están registrados. El aviso de cuenta bloqueada solo
     * se da con la contraseña correcta, por el mismo motivo.</p>
     *
     * <p>Tras {@link LimiteIntentosLogin#MAX_FALLOS} fallos seguidos con el mismo correo, lanza
     * {@link #DEMASIADOS_INTENTOS} sin comprobar la contraseña hasta que pase el bloqueo. Se cuenta
     * igual exista o no el correo, para no delatarlo.</p>
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String email, String contrasena) throws Exception {
        long minutos = limiteIntentos.minutosBloqueado(email);
        if (minutos > 0) {
            throw new Exception(String.format(DEMASIADOS_INTENTOS, minutos, minutos == 1 ? "minuto" : "minutos"));
        }
        Optional<Usuario> usuario = buscarPorCorreo(email);
        boolean correcta = usuario.isPresent()
                ? Contrasenas.coincide(contrasena, usuario.get().getPassword())
                : Contrasenas.compararFicticio(contrasena);
        if (!correcta) {
            limiteIntentos.registrarFallo(email);
            return Optional.empty();
        }
        limiteIntentos.reiniciar(email);
        if (usuario.get().getEstado() != Estado.ACTIVO) {
            throw new Exception(CUENTA_BLOQUEADA);
        }
        return usuario;
    }

    /** Un correo solo puede estar en una de las tres tablas de usuarios. */
    private Optional<Usuario> buscarPorCorreo(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String correo = email.trim();
        Optional<Usuario> usuario = administradorRepo.buscarAdministradorPorCorreo(correo).map(u -> (Usuario) u);
        if (usuario.isEmpty()) {
            usuario = escritorRepo.buscarEscritorPorCorreo(correo).map(u -> (Usuario) u);
        }
        if (usuario.isEmpty()) {
            usuario = lectorRepo.buscarLectorPorCorreo(correo).map(u -> (Usuario) u);
        }
        return usuario;
    }
}
