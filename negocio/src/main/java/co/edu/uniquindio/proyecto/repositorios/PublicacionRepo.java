package co.edu.uniquindio.proyecto.repositorios;


import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicacionRepo extends JpaRepository<Publicacion, Long> {
    @Query("SELECT p FROM Publicacion p WHERE p.estado = 'APROBADO' AND p.obraLiteraria.id = :obraId")
    List<Publicacion> recuperarPublicacionesAprobadasDeObra(@Param("obraId") Long obraId);
    @Query("SELECT p FROM Publicacion p WHERE p.estado = :estado ORDER BY p.fechaPublicacion")
    List<Publicacion> buscarPublicacionesPorEstadoYFecha(@Param("estado") String estado);
    @Query("SELECT DISTINCT p FROM Publicacion p JOIN p.comentarios c WHERE c.estado = 'APROBADO'")
    List<Publicacion> listarPublicacionesConComentariosAprobados();
    @Query("SELECT p FROM Publicacion p WHERE LOWER(p.titulo) LIKE LOWER(:palabraClave)")
    List<Publicacion> obtenerPublicacionesPorTitulo(@Param("palabraClave") String palabraClave);
    @Query("SELECT COUNT(p) FROM Publicacion p WHERE p.estado = 'RECHAZADO'")
    Long contarPublicacionesRechazadas();
    @Query("SELECT p FROM Publicacion p WHERE LOWER(p.contenido) LIKE LOWER(:palabraClave)")
    List<Publicacion> buscarPublicacionesPorContenido(@Param("palabraClave") String palabraClave);
    @Query("SELECT p, COUNT(lpv) AS numVistas FROM Publicacion p LEFT JOIN p.lectoresVisitas lpv GROUP BY p ORDER BY numVistas DESC")
    List<Object[]> listarPublicacionesOrdenadasPorVistas();
    @Query("SELECT p FROM Publicacion p WHERE (SELECT COUNT(c) FROM p.comentarios c WHERE c.estado = 'APROBADO') > :numeroComentarios")
    List<Publicacion> obtenerPublicacionesConMasComentariosAprobados(@Param("numeroComentarios") Long numeroComentarios);
    @Query("SELECT p FROM Publicacion p WHERE p.estado = 'PUBLICADO' AND (LOWER(p.titulo) LIKE %:frase% OR LOWER(p.contenido) LIKE %:frase%)")
    List<Publicacion> obtenerPublicacionesPorFrase(@Param("frase") String frase);
    // Una obra puede tener coautores, así que se devuelve una lista (antes Optional, que fallaba con más de un autor)
    @Query("SELECT e FROM Escritor e JOIN e.obrasLiterarias ol WHERE ol.id = :obraLiterariaId ORDER BY e.id")
    List<Escritor> obtenerEscritoresDeObra(@Param("obraLiterariaId") Long obraLiterariaId);

    @Query("SELECT p FROM Publicacion p WHERE p.estado = 'PUBLICADO' ORDER BY p.fechaPublicacion DESC, p.id DESC")
    List<Publicacion> listarPublicacionesPublicadas();

    // ---- Interacción del lector (Fase 6) ----

    @Query("SELECT COUNT(l) FROM Lector l JOIN l.publicacionesRecomendadas p WHERE p.id = :publicacionId")
    long contarRecomendaciones(@Param("publicacionId") Long publicacionId);

    @Query("SELECT COUNT(l) FROM Lector l JOIN l.publicacionesVistas p WHERE p.id = :publicacionId")
    long contarVistas(@Param("publicacionId") Long publicacionId);

    @Query("SELECT COUNT(l) > 0 FROM Lector l JOIN l.publicacionesRecomendadas p WHERE l.id = :lectorId AND p.id = :publicacionId")
    boolean haRecomendado(@Param("lectorId") Long lectorId, @Param("publicacionId") Long publicacionId);

    @Query("SELECT p.id, COUNT(l) FROM Publicacion p JOIN p.lectoresVisitas l GROUP BY p.id")
    List<Object[]> contarVistasPorPublicacion();

    @Query("SELECT p.id, COUNT(l) FROM Publicacion p JOIN p.lectoresRecomendaciones l GROUP BY p.id")
    List<Object[]> contarRecomendacionesPorPublicacion();

    /** Búsqueda en título y descripción; {@code patron} ya trae los comodines y usa ! como carácter de escape. */
    @Query("SELECT p FROM Publicacion p WHERE p.estado = 'PUBLICADO' " +
            "AND (LOWER(p.titulo) LIKE :patron ESCAPE '!' OR LOWER(p.contenido) LIKE :patron ESCAPE '!')")
    List<Publicacion> buscarPublicadas(@Param("patron") String patron);

    @Query("SELECT DISTINCT p FROM Publicacion p JOIN p.obraLiteraria o JOIN o.generos g WHERE p.estado = 'PUBLICADO' " +
            "AND g.id = :generoId AND (LOWER(p.titulo) LIKE :patron ESCAPE '!' OR LOWER(p.contenido) LIKE :patron ESCAPE '!')")
    List<Publicacion> buscarPublicadasPorGenero(@Param("patron") String patron, @Param("generoId") Long generoId);

    /** Publicaciones de alguno de los géneros o escritores indicados (las listas no pueden estar vacías). */
    @Query("SELECT DISTINCT p FROM Publicacion p JOIN p.obraLiteraria o LEFT JOIN o.generos g LEFT JOIN o.escritores e " +
            "WHERE p.estado = 'PUBLICADO' AND (g.id IN :generos OR e.id IN :escritores)")
    List<Publicacion> buscarPublicadasPorGenerosOEscritores(@Param("generos") List<Long> generos,
                                                            @Param("escritores") List<Long> escritores);
}
