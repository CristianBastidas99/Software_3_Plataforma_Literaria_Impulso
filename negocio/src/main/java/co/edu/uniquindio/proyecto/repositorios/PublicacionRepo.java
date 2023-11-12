package co.edu.uniquindio.proyecto.repositorios;


import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    @Query("SELECT p FROM Publicacion p WHERE p.estado = 'APROBADO' AND (LOWER(p.titulo) LIKE LOWER(:frase) OR LOWER(p.contenido) LIKE LOWER(:frase))")
    List<Publicacion> obtenerPublicacionesPorFrase(@Param("frase") String frase);


}
