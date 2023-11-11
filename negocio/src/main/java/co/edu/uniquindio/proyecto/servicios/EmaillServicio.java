package co.edu.uniquindio.proyecto.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmaillServicio {

    @Autowired
    private JavaMailSender sender;

    public boolean enviarEmail(String asunto, String contenido, String destinatario){
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setText(contenido,true);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            //helper.setFrom();

            sender.send(message);

            return true;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
