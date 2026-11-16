package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import co.edu.uniquindio.proyecto.repositorios.TokenRecuperacionRepo;
import co.edu.uniquindio.proyecto.servicios.CorreoServicio;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.RecuperacionServicio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Flujo de recuperación de contraseña. El envío de correos se simula para no depender del SMTP. */
@SpringBootTest
@Transactional
@Sql("classpath:pli.sql")
public class RecuperacionServicioTest {

    private static final String ENLACE_INVALIDO = "El enlace para cambiar la contraseña no es válido, ya se usó o caducó. Pide uno nuevo.";

    @Autowired
    private RecuperacionServicio recuperacion;
    @Autowired
    private TokenRecuperacionRepo tokenRepo;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @MockBean
    private CorreoServicio correoServicio;

    @Test
    public void solicitarEnviaEnlaceSoloACuentasActivas() {
        when(correoServicio.getUrlBase()).thenReturn("http://impulso.test");

        recuperacion.solicitarRecuperacion("writer1@example.com");
        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correoServicio).enviarRecuperacion(eq("Escritor Uno"), eq("writer1@example.com"), enlace.capture(), eq(30));
        assertTrue(enlace.getValue().startsWith("http://impulso.test/restablecer.xhtml?token="));

        // El token del enlace es válido, y en la base de datos solo está su hash
        String token = enlace.getValue().substring(enlace.getValue().indexOf("token=") + 6);
        assertDoesNotThrow(() -> recuperacion.validarToken(token));
        assertTrue(tokenRepo.findAll().stream().noneMatch(t -> t.getTokenHash().equals(token)));

        // Correo no registrado o cuenta bloqueada: no se envía nada (y no hay error que lo delate)
        clearInvocations(correoServicio);
        recuperacion.solicitarRecuperacion("nadie@example.com");
        recuperacion.solicitarRecuperacion("writer3@example.com");
        recuperacion.solicitarRecuperacion("  ");
        verify(correoServicio, never()).enviarRecuperacion(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    public void restablecerCambiaLaContrasenaYElEnlaceSirveUnaVez() throws Exception {
        String token = recuperacion.crearToken("reader1@example.com").orElseThrow();
        assertEquals("reader1@example.com", recuperacion.validarToken(token));

        Exception e = assertThrows(Exception.class, () -> recuperacion.restablecerContrasena(token, "corta", "corta"));
        assertTrue(e.getMessage().contains("entre 8 y 30"));
        e = assertThrows(Exception.class, () -> recuperacion.restablecerContrasena(token, "nuevaClave1", "otraClave1"));
        assertEquals("Las contraseñas no coinciden", e.getMessage());

        recuperacion.restablecerContrasena(token, "nuevaClave1", "nuevaClave1");
        assertNotNull(lectorServicio.validaLogin("reader1@example.com", "nuevaClave1"));
        assertNull(lectorServicio.validaLogin("reader1@example.com", "readerpass"));

        e = assertThrows(Exception.class, () -> recuperacion.restablecerContrasena(token, "otraMas123", "otraMas123"));
        assertEquals(ENLACE_INVALIDO, e.getMessage());
        assertThrows(Exception.class, () -> recuperacion.validarToken(token));
    }

    @Test
    public void pedirOtroEnlaceInvalidaElAnterior() throws Exception {
        String primero = recuperacion.crearToken("writer2@example.com").orElseThrow();
        String segundo = recuperacion.crearToken("WRITER2@example.com").orElseThrow();
        assertNotEquals(primero, segundo);
        assertThrows(Exception.class, () -> recuperacion.validarToken(primero));
        recuperacion.restablecerContrasena(segundo, "clave-nueva", "clave-nueva");
        assertNotNull(escritorServicio.validaLogin("writer2@example.com", "clave-nueva"));
    }

    @Test
    public void enlaceCaducadoOInventadoNoSirve() {
        String token = recuperacion.crearToken("writer4@example.com").orElseThrow();
        List<TokenRecuperacion> tokens = tokenRepo.findAll();
        tokens.forEach(t -> t.setFechaExpiracion(LocalDateTime.now().minusMinutes(1)));
        tokenRepo.saveAll(tokens);

        Exception e = assertThrows(Exception.class, () -> recuperacion.validarToken(token));
        assertEquals(ENLACE_INVALIDO, e.getMessage());
        assertThrows(Exception.class, () -> recuperacion.restablecerContrasena(token, "clave-nueva", "clave-nueva"));
        assertThrows(Exception.class, () -> recuperacion.validarToken("inventado"));
        assertThrows(Exception.class, () -> recuperacion.validarToken(null));
        assertTrue(recuperacion.crearToken("reader3@example.com").isEmpty());
    }
}
