package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.repositorios.TokenRecuperacionRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class RecuperacionServicioImpl implements RecuperacionServicio {

    private static final String ENLACE_INVALIDO = "El enlace para cambiar la contraseña no es válido, ya se usó o caducó. Pide uno nuevo.";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final TokenRecuperacionRepo tokenRepo;
    private final AdministradorRepo administradorRepo;
    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;
    private final CorreoServicio correoServicio;
    private final int minutosValidez;

    public RecuperacionServicioImpl(TokenRecuperacionRepo tokenRepo, AdministradorRepo administradorRepo,
                                    EscritorRepo escritorRepo, LectorRepo lectorRepo, CorreoServicio correoServicio,
                                    @Value("${app.recuperacion.minutos:30}") int minutosValidez) {
        this.tokenRepo = tokenRepo;
        this.administradorRepo = administradorRepo;
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
        this.correoServicio = correoServicio;
        this.minutosValidez = minutosValidez;
    }

    @Override
    public int getMinutosValidez() {
        return minutosValidez;
    }

    @Override
    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuario = usuarioActivo(email);
        if (usuario.isEmpty()) {
            return;
        }
        String token = nuevoToken(usuario.get().getEmail());
        String enlace = correoServicio.getUrlBase() + "/restablecer.xhtml?token=" + token;
        correoServicio.enviarRecuperacion(usuario.get().getNombre(), usuario.get().getEmail(), enlace, minutosValidez);
    }

    @Override
    public Optional<String> crearToken(String email) {
        return usuarioActivo(email).map(u -> nuevoToken(u.getEmail()));
    }

    @Override
    @Transactional(readOnly = true)
    public String validarToken(String token) throws Exception {
        return tokenVigente(token).getEmail();
    }

    @Override
    public void restablecerContrasena(String token, String nueva, String confirmacion) throws Exception {
        TokenRecuperacion registro = tokenVigente(token);
        if (nueva == null || nueva.isEmpty()) {
            throw new Exception("La contraseña nueva no puede estar vacía");
        }
        if (nueva.length() < PASSWORD_MIN || nueva.length() > PASSWORD_MAX) {
            throw new Exception("La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres");
        }
        if (!nueva.equals(confirmacion)) {
            throw new Exception("Las contraseñas no coinciden");
        }
        String email = registro.getEmail();
        if (!cambiarPassword(email, nueva)) {
            throw new Exception("La cuenta de este enlace ya no existe");
        }
        tokenRepo.invalidarTokensDe(email);
    }

    /** Un correo solo puede estar en una de las tres tablas de usuarios. */
    private boolean cambiarPassword(String email, String nueva) {
        Optional<Administrador> administrador = administradorRepo.buscarAdministradorPorCorreo(email);
        if (administrador.isPresent()) {
            administrador.get().setPassword(nueva);
            administradorRepo.save(administrador.get());
            return true;
        }
        Optional<Escritor> escritor = escritorRepo.buscarEscritorPorCorreo(email);
        if (escritor.isPresent()) {
            escritor.get().setPassword(nueva);
            escritorRepo.save(escritor.get());
            return true;
        }
        Optional<Lector> lector = lectorRepo.buscarLectorPorCorreo(email);
        if (lector.isPresent()) {
            lector.get().setPassword(nueva);
            lectorRepo.save(lector.get());
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    private Optional<Usuario> usuarioActivo(String email) {
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
        // Un usuario bloqueado no puede recuperar el acceso por esta vía
        return usuario.filter(u -> u.getEstado() == Estado.ACTIVO);
    }

    private String nuevoToken(String email) {
        tokenRepo.invalidarTokensDe(email);
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime ahora = LocalDateTime.now();
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setTokenHash(hash(token));
        registro.setEmail(email);
        registro.setFechaCreacion(ahora);
        registro.setFechaExpiracion(ahora.plusMinutes(minutosValidez));
        registro.setUsado(false);
        tokenRepo.save(registro);
        return token;
    }

    private TokenRecuperacion tokenVigente(String token) throws Exception {
        if (token == null || token.isBlank() || token.length() > 100) {
            throw new Exception(ENLACE_INVALIDO);
        }
        return tokenRepo.findByTokenHash(hash(token.trim()))
                .filter(t -> t.isVigente(LocalDateTime.now()))
                .orElseThrow(() -> new Exception(ENLACE_INVALIDO));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
