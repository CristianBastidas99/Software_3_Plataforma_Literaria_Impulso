package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.Estadisticas;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;

import java.util.List;

/**
 * Operaciones del panel de administración. El servicio no conoce la sesión: los beans comprueban
 * que el usuario sea administrador antes de llamarlo.
 *
 * <p>Convenciones de estado: un usuario bloqueado está {@code INACTIVO} (no puede iniciar sesión); una
 * publicación ocultada por el administrador queda {@code RECHAZADO} junto con su obra (su autor no
 * puede volver a publicarla), a diferencia de una retirada por el autor ({@code INACTIVO}); un
 * comentario ocultado queda {@code RECHAZADO}.</p>
 */
public interface AdministracionServicio {

    int GENERO_MAX = 50;

    // ---- Usuarios ----

    List<Escritor> listarEscritores();

    List<Lector> listarLectores();

    /** Bloquea ({@code activo = false}) o desbloquea a un escritor. */
    void cambiarEstadoEscritor(Long idEscritor, boolean activo) throws Exception;

    void cambiarEstadoLector(Long idLector, boolean activo) throws Exception;

    // ---- Publicaciones ----

    /** Todas las publicaciones, en cualquier estado, de la más reciente a la más antigua. */
    List<Publicacion> listarPublicaciones();

    void ocultarPublicacion(Long idPublicacion) throws Exception;

    /** Vuelve a publicar una publicación ocultada por el administrador. */
    void mostrarPublicacion(Long idPublicacion) throws Exception;

    /**
     * Elimina la publicación y su obra con todo lo que depende de ellas (fragmento, comentarios,
     * vistas, recomendaciones). Devuelve las rutas de la portada y del PDF para que el llamador
     * borre los archivos.
     */
    List<String> eliminarPublicacion(Long idPublicacion) throws Exception;

    // ---- Comentarios ----

    /** Todos los comentarios, en cualquier estado, del más reciente al más antiguo. */
    List<Comentario> listarComentarios();

    void ocultarComentario(Long idComentario) throws Exception;

    void mostrarComentario(Long idComentario) throws Exception;

    void eliminarComentario(Long idComentario) throws Exception;

    // ---- Géneros ----

    /** Géneros ordenados alfabéticamente. */
    List<Genero> listarGeneros();

    Genero crearGenero(String nombre) throws Exception;

    Genero renombrarGenero(Long idGenero, String nombre) throws Exception;

    /** Elimina un género sin obras; también lo quita de las preferencias de los lectores. */
    void eliminarGenero(Long idGenero) throws Exception;

    long contarObrasDeGenero(Long idGenero);

    // ---- Estadísticas ----

    Estadisticas obtenerEstadisticas(int maxMasVistas);
}
