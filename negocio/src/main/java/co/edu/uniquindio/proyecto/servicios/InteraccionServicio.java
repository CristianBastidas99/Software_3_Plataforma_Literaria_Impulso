package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.entidades.Usuario;

import java.util.List;

/**
 * Interacción de los usuarios con las publicaciones y los escritores: comentarios, recomendaciones,
 * vistas, escritores favoritos, géneros preferidos y búsqueda con filtros.
 *
 * <p>Los errores se lanzan como {@link Exception} con un mensaje listo para mostrar al usuario.</p>
 */
public interface InteraccionServicio {

    /** Orden de los resultados de búsqueda. */
    enum Orden { RECIENTES, POPULARES }

    int COMENTARIO_MAX = 255;

    // ---- Comentarios ----

    /**
     * Publica un comentario de un lector o un escritor en una publicación publicada. Las palabras
     * ofensivas se censuran con asteriscos.
     */
    Comentario comentar(Long idPublicacion, Usuario autor, String texto) throws Exception;

    /** Elimina un comentario; solo puede hacerlo su autor. */
    void eliminarComentario(Long idComentario, Usuario solicitante) throws Exception;

    /** Comentarios visibles (aprobados) de una publicación, del más reciente al más antiguo. */
    List<Comentario> listarComentarios(Long idPublicacion);

    /** Indica si el usuario es el autor del comentario (el lector, o el escritor si no hay lector). */
    boolean esAutorDeComentario(Comentario comentario, Usuario usuario);

    // ---- Recomendaciones y vistas ----

    /** Activa o desactiva la recomendación del lector. Devuelve el nuevo estado. */
    boolean alternarRecomendacion(Long idLector, Long idPublicacion) throws Exception;

    boolean haRecomendado(Long idLector, Long idPublicacion);

    long contarRecomendaciones(Long idPublicacion);

    /** Registra que el lector vio la publicación; si ya la había visto no hace nada. */
    void registrarVista(Long idLector, Long idPublicacion);

    long contarVistas(Long idPublicacion);

    // ---- Escritores favoritos ----

    /** Marca o desmarca al escritor como favorito del lector. Devuelve el nuevo estado. */
    boolean alternarFavorito(Long idLector, Long idEscritor) throws Exception;

    boolean esFavorito(Long idLector, Long idEscritor);

    long contarSeguidores(Long idEscritor);

    // ---- Géneros preferidos y recomendaciones personalizadas ----

    List<Long> obtenerGenerosPreferidos(Long idLector);

    void actualizarGenerosPreferidos(Long idLector, List<Long> idsGeneros) throws Exception;

    /**
     * Publicaciones de los géneros preferidos o de los escritores favoritos del lector, primero las
     * que aún no ha visto y después las más populares. Vacía si el lector no tiene preferencias.
     */
    List<Publicacion> recomendadasPara(Long idLector, int maximo);

    // ---- Búsqueda ----

    /**
     * Publicaciones publicadas que contienen la frase (en título o descripción) y son del género
     * indicado. La frase y el género son opcionales.
     */
    List<Publicacion> buscarPublicaciones(String frase, Long idGenero, Orden orden);
}
