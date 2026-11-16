package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.servicios.CorreoServicio;
import co.edu.uniquindio.proyecto.servicios.EmaillServicio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Pruebas sin Spring ni servidor SMTP. */
public class CorreoServicioTest {

    @Test
    public void unFalloDelSmtpNoSePropaga() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new MailSendException("Connection refused")).when(sender).send(any(MimeMessage.class));

        EmaillServicio servicio = new EmaillServicio(sender, "Impulso <no-responder@impulso.com>");
        assertFalse(servicio.enviarEmail("Asunto", "<p>Hola</p>", "alguien@example.com"));
    }

    @Test
    public void elCorreoLlevaRemitenteYAcentos() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());

        EmaillServicio servicio = new EmaillServicio(sender, "Impulso <no-responder@impulso.com>");
        assertTrue(servicio.enviarEmail("Contraseña", "<p>Árbol</p>", "alguien@example.com"));

        ArgumentCaptor<MimeMessage> enviado = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(enviado.capture());
        assertEquals("Impulso <no-responder@impulso.com>", enviado.getValue().getFrom()[0].toString());
        assertEquals("Contraseña", enviado.getValue().getSubject());
    }

    @Test
    public void losTextosDelUsuarioSeEscapan() {
        EmaillServicio email = mock(EmaillServicio.class);
        CorreoServicio correo = new CorreoServicio(email, "http://impulso.test/");
        assertEquals("http://impulso.test", correo.getUrlBase());

        correo.enviarAvisoComentario("Ana", "ana@example.com", "<script>x</script>", "Título & más",
                "Me \"encantó\"", 7L);
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        verify(email).enviarEmail(anyString(), html.capture(), eq("ana@example.com"));
        assertFalse(html.getValue().contains("<script>"));
        assertTrue(html.getValue().contains("&lt;script&gt;x&lt;/script&gt;"));
        assertTrue(html.getValue().contains("Título &amp; más"));
        assertTrue(html.getValue().contains("http://impulso.test/publicacion.xhtml?idProd=7"));
    }
}
