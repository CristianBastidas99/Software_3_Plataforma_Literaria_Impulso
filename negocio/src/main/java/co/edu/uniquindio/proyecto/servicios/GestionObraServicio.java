package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.DatosObra;
import co.edu.uniquindio.proyecto.entidades.ObraLiteraria;

import java.util.List;

/**
 * Operaciones del escritor sobre sus propias obras. Cada operación comprueba que el escritor
 * esté activo y sea autor (o coautor) de la obra, de modo que no se puede modificar una obra ajena
 * aunque se manipule la URL o la petición.
 *
 * <p>Los errores se lanzan como {@link Exception} con un mensaje listo para mostrar al usuario.</p>
 */
public interface GestionObraServicio {

    /** Crea la obra, su fragmento y su publicación en una sola transacción. */
    ObraLiteraria crearObra(Long idAutor, DatosObra datos) throws Exception;

    /** Actualiza los datos de una obra del autor. El autor que edita sigue siempre entre los firmantes. */
    ObraLiteraria actualizarObra(Long idAutor, Long idObra, DatosObra datos) throws Exception;

    /** Devuelve la obra si existe y el escritor es uno de sus autores. */
    ObraLiteraria obtenerObraDeAutor(Long idAutor, Long idObra) throws Exception;

    /** Datos de una obra del autor listos para el formulario de edición (los coautores no incluyen al propio autor). */
    DatosObra obtenerDatosObra(Long idAutor, Long idObra) throws Exception;

    /** Obras en las que participa el escritor, de la más reciente a la más antigua. */
    List<ObraLiteraria> listarObrasDeAutor(Long idAutor);

    /** Retira la obra: deja de verse en inicio, búsquedas y perfiles, pero no se borra. */
    void retirarObra(Long idAutor, Long idObra) throws Exception;

    /** Vuelve a publicar una obra retirada. */
    void volverAPublicarObra(Long idAutor, Long idObra) throws Exception;

    boolean esAutorDeObra(Long idAutor, Long idObra);
}
