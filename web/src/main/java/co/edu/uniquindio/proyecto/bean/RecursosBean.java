package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import org.springframework.stereotype.Component;

/**
 * Convierte las rutas de imágenes y PDFs guardadas en la base de datos en URLs para las vistas.
 *
 * <ul>
 *   <li>{@code uploads/...}: archivo subido por un usuario, servido desde la carpeta externa.</li>
 *   <li>Cualquier otro nombre: recurso estático de la aplicación ({@code resources/img} o {@code resources/pdf}),
 *       como los de la demo.</li>
 *   <li>Vacío: imagen por defecto.</li>
 * </ul>
 *
 * <p>Las URLs empiezan por {@code /} y no incluyen el context path: {@code h:graphicImage},
 * {@code p:graphicImage} y {@code p:media} lo añaden solos. En enlaces ({@code h:outputLink})
 * hay que anteponer {@code #{request.contextPath}}.</p>
 */
@Component("recursos")
public class RecursosBean {

    public static final String IMAGEN_USUARIO_DEFECTO = "autor.jpg";
    public static final String IMAGEN_PUBLICACION_DEFECTO = "imagen.jpg";

    private final ArchivoServicio archivoServicio;

    public RecursosBean(ArchivoServicio archivoServicio) {
        this.archivoServicio = archivoServicio;
    }

    /** URL de la imagen de perfil de un usuario (escritor, lector o administrador). */
    public String imagenUsuario(String ruta) {
        return url(ruta, "/resources/img/", IMAGEN_USUARIO_DEFECTO);
    }

    /** URL de la portada de una publicación. */
    public String imagenPublicacion(String ruta) {
        return url(ruta, "/resources/img/", IMAGEN_PUBLICACION_DEFECTO);
    }

    /** URL del PDF de un fragmento, o {@code null} si no tiene. */
    public String pdf(String ruta) {
        return url(ruta, "/resources/pdf/", null);
    }

    public long getMaxImagenBytes() {
        return archivoServicio.getMaxImagenBytes();
    }

    public long getMaxPdfBytes() {
        return archivoServicio.getMaxPdfBytes();
    }

    public long getMaxImagenMb() {
        return archivoServicio.getMaxImagenBytes() / (1024 * 1024);
    }

    public long getMaxPdfMb() {
        return archivoServicio.getMaxPdfBytes() / (1024 * 1024);
    }

    private static String url(String ruta, String carpetaEstatica, String porDefecto) {
        if (ruta == null || ruta.isBlank()) {
            return porDefecto == null ? null : carpetaEstatica + porDefecto;
        }
        if (ruta.startsWith(ArchivoServicio.PREFIJO)) {
            return "/" + ruta;
        }
        return carpetaEstatica + ruta;
    }
}
