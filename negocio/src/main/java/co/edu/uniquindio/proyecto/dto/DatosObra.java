package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.validacion.Isbn;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Datos que un escritor introduce para crear o editar una obra. Con ellos se construyen la
 * {@code ObraLiteraria}, su {@code Fragmento} y su {@code Publicacion}.
 *
 * <p>Las restricciones coinciden con la longitud de las columnas. Las comprueban tanto el formulario
 * JSF (cada error aparece junto a su campo) como {@code GestionObraServicio} antes de guardar.</p>
 */
@Getter
@Setter
public class DatosObra implements Serializable {

    public static final int TITULO_MAX = 70;
    public static final int SINOPSIS_MAX = 150;
    public static final int DESCRIPCION_MAX = 255;
    public static final int EDITORIAL_MAX = 100;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = TITULO_MAX, message = "El título no puede tener más de " + TITULO_MAX + " caracteres")
    private String titulo;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(max = SINOPSIS_MAX, message = "La sinopsis no puede tener más de " + SINOPSIS_MAX + " caracteres")
    private String sinopsis;

    /** Texto de presentación de la publicación (columna {@code contenido}). */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = DESCRIPCION_MAX, message = "La descripción no puede tener más de " + DESCRIPCION_MAX + " caracteres")
    private String descripcion;

    @NotBlank(message = "El ISBN es obligatorio")
    @Isbn
    private String isbn;

    @Size(max = EDITORIAL_MAX, message = "La editorial no puede tener más de " + EDITORIAL_MAX + " caracteres")
    private String editorial;

    @NotNull(message = "La fecha de publicación es obligatoria")
    @PastOrPresent(message = "La fecha de publicación no puede ser futura")
    private Date fechaPublicacion;

    @NotEmpty(message = "Elige al menos un género")
    private List<Long> generos = new ArrayList<>();

    /** Otros escritores que firman la obra, además del autor que la crea o edita. */
    private List<Long> coautores = new ArrayList<>();

    /** Ruta de la portada (vacía para usar la imagen por defecto). */
    private String urlPortada;

    /** Ruta del PDF del fragmento (obligatoria). */
    private String urlFragmento;
}
