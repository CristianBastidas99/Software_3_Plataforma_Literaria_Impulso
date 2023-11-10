package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepo extends JpaRepository<Comentario, Long> {
    @Query("SELECT c FROM Comentario c WHERE c.estado = 'APROBADO'")
    List<Comentario> recuperarComentariosAprobados();
    @Query("SELECT c FROM Comentario c WHERE c.escritor.id = :escritorId")
    List<Comentario> obtenerComentariosDeEscritor(@Param("escritorId") Long escritorId);
    @Query("SELECT c FROM Comentario c WHERE c.publicacion.id = :publicacionId")
    List<Comentario> buscarComentariosEnPublicacion(@Param("publicacionId") Long publicacionId);
    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.estado = 'PENDIENTE'")
    Long contarComentariosPendientes();
    @Query("SELECT c FROM Comentario c WHERE c.lector.id = :lectorId")
    List<Comentario> listarComentariosDeLector(@Param("lectorId") Long lectorId);
    @Query("SELECT c FROM Comentario c WHERE c.estado = 'RECHAZADO' AND c.publicacion.obraLiteraria.id = :obraId")
    List<Comentario> obtenerComentariosRechazadosEnObra(@Param("obraId") Long obraId);
    @Query("SELECT c FROM Comentario c WHERE LOWER(c.contenido) LIKE LOWER(:palabraClave)")
    List<Comentario> buscarComentariosPorContenido(@Param("palabraClave") String palabraClave);
    @Query("SELECT c FROM Comentario c ORDER BY c.fechaPublicacion DESC")
    List<Comentario> listarComentariosMasRecientes();

}
