package co.edu.uniquindio.proyecto.servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Correos que envía la aplicación. Cada método se ejecuta en segundo plano ({@code @Async}): quien
 * lo llama sigue de inmediato y, si el SMTP falla, el error solo queda en el log.
 *
 * <p>Los textos que vienen del usuario (nombres, títulos, comentarios) se escapan antes de
 * insertarlos en el HTML.</p>
 */
@Service
public class CorreoServicio {

    private final EmaillServicio emaillServicio;
    private final String urlBase;
    private final String plantilla;

    public CorreoServicio(EmaillServicio emaillServicio, @Value("${app.url-base:http://localhost:8080}") String urlBase) {
        this.emaillServicio = emaillServicio;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.plantilla = leerPlantilla();
    }

    /** Dirección pública de la aplicación, sin barra final (para construir enlaces). */
    public String getUrlBase() {
        return urlBase;
    }

    @Async
    public void enviarBienvenida(String nombre, String email, boolean esEscritor) {
        String queHacer = esEscritor
                ? "Ya puedes publicar tus obras desde <strong>Nueva obra</strong> y seguir cómo las reciben tus lectores."
                : "Ya puedes explorar obras, comentarlas, recomendarlas y seguir a tus escritores favoritos.";
        String cuerpo = parrafo("Hola, " + escapar(nombre) + ":")
                + parrafo("Te damos la bienvenida a Plataforma Literaria Impulso. " + queHacer)
                + boton("Iniciar sesión", urlBase + "/login.xhtml");
        enviar("Bienvenido a Plataforma Literaria Impulso", "¡Bienvenido a Impulso!", cuerpo, email);
    }

    @Async
    public void enviarRecuperacion(String nombre, String email, String enlace, int minutosValidez) {
        String cuerpo = parrafo("Hola, " + escapar(nombre) + ":")
                + parrafo("Recibimos una solicitud para cambiar la contraseña de tu cuenta. Pulsa el botón para elegir una nueva.")
                + boton("Cambiar mi contraseña", enlace)
                + parrafo("El enlace sirve una sola vez y caduca en " + minutosValidez + " minutos.")
                + parrafo("<small>Si no lo pediste tú, ignora este correo: tu contraseña no cambiará.</small>")
                + parrafo("<small>Si el botón no funciona, copia esta dirección en el navegador:<br>" + escapar(enlace) + "</small>");
        enviar("Cambia tu contraseña de Impulso", "Cambia tu contraseña", cuerpo, email);
    }

    @Async
    public void enviarAvisoComentario(String nombreEscritor, String email, String autorComentario,
                                      String tituloPublicacion, String comentario, Long idPublicacion) {
        String cuerpo = parrafo("Hola, " + escapar(nombreEscritor) + ":")
                + parrafo(escapar(autorComentario) + " comentó tu obra <strong>" + escapar(tituloPublicacion) + "</strong>:")
                + "<blockquote style=\"margin:0 0 16px; padding:10px 16px; background:#f7f4ef; border-left:4px solid #b08d57; white-space:pre-wrap;\">"
                + escapar(comentario) + "</blockquote>"
                + boton("Ver la publicación", urlBase + "/publicacion.xhtml?idProd=" + idPublicacion);
        enviar("Nuevo comentario en «" + tituloPublicacion + "»", "Tienes un comentario nuevo", cuerpo, email);
    }

    // ---------------------------------------------------------------------------------------------

    private void enviar(String asunto, String titulo, String cuerpo, String destinatario) {
        String html = plantilla.replace("{{titulo}}", escapar(titulo)).replace("{{cuerpo}}", cuerpo);
        emaillServicio.enviarEmail(asunto, html, destinatario);
    }

    private static String parrafo(String html) {
        return "<p style=\"margin:0 0 16px;\">" + html + "</p>";
    }

    private static String boton(String texto, String enlace) {
        return "<p style=\"margin:24px 0;\"><a href=\"" + escapar(enlace) + "\" style=\"background:#b08d57; color:#ffffff; "
                + "padding:12px 22px; border-radius:6px; text-decoration:none; font-family:Arial, sans-serif;\">"
                + escapar(texto) + "</a></p>";
    }

    static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String leerPlantilla() {
        try {
            return StreamUtils.copyToString(new ClassPathResource("correos/plantilla.html").getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se encontró la plantilla de correo correos/plantilla.html", e);
        }
    }
}
