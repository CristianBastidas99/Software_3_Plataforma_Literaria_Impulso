package co.edu.uniquindio.proyecto.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

/**
 * Envío de bajo nivel de un correo HTML. Es síncrono: los correos de la aplicación se envían con
 * {@link CorreoServicio}, que llama a este servicio en segundo plano.
 */
@Service
public class EmaillServicio {

    private static final Logger LOG = LoggerFactory.getLogger(EmaillServicio.class);

    private final JavaMailSender sender;
    private final String remitente;

    public EmaillServicio(JavaMailSender sender,
                          @Value("${app.correo.remitente:Plataforma Literaria Impulso <no-responder@impulso.com>}") String remitente) {
        this.sender = sender;
        this.remitente = remitente;
    }

    /**
     * Envía el correo y devuelve si se pudo. Un fallo del servidor SMTP no se propaga: queda en el
     * log, para que la acción que lo provocó (registrarse, comentar...) no falle por el correo.
     */
    public boolean enviarEmail(String asunto, String contenido, String destinatario) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);
            sender.send(message);
            return true;
        } catch (MessagingException | MailException e) {
            LOG.error("No se pudo enviar el correo «{}» a {}: {}", asunto, destinatario, e.getMessage());
            return false;
        }
    }
}
