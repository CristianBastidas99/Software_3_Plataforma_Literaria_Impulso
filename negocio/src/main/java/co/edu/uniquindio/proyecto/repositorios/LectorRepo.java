package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.entidades.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectorRepo extends JpaRepository<Lector, Long> {
    @Query("SELECT l FROM Lector l WHERE l.email = :correoElectronico AND l.password = :contrasena")
    Optional<Lector> iniciarSesionLector(@Param("correoElectronico") String correoElectronico, @Param("contrasena") String contrasena);
    @Query("SELECT l FROM Lector l WHERE l.estado = 'ACTIVO'")
    List<Lector> obtenerLectoresActivos();
    @Query("SELECT l FROM Lector l WHERE l.email = :correoElectronico")
    Optional<Lector> buscarLectorPorCorreo(@Param("correoElectronico") String correoElectronico);
    @Query("SELECT COUNT(l) FROM Lector l")
    Long contarLectores();
    @Query("SELECT l FROM Lector l WHERE LOWER(l.nombre) LIKE LOWER(:nombre)")
    List<Lector> buscarLectoresPorNombre(@Param("nombre") String nombre);
    @Query("SELECT l FROM Lector l WHERE l.estado = :estado ORDER BY l.id")
    List<Lector> obtenerLectoresPorEstado(@Param("estado") String estado);
    @Query("SELECT l.escritoresFavoritos FROM Lector l WHERE l.id = :lectorId")
    List<Escritor> obtenerEscritoresFavoritosDeLector(@Param("lectorId") Long lectorId);
    @Query("SELECT l.publicacionesRecomendadas FROM Lector l WHERE l.id = :PublicacionId")
    List<Publicacion> obtenerPublicacionesRecomendadas(@Param("PublicacionId") Long PublicacionId);

    // ---- Interacción del lector (Fase 6) ----

    @Query("SELECT COUNT(l) FROM Lector l JOIN l.escritoresFavoritos e WHERE e.id = :escritorId")
    long contarSeguidores(@Param("escritorId") Long escritorId);

    @Query("SELECT COUNT(l) > 0 FROM Lector l JOIN l.escritoresFavoritos e WHERE l.id = :lectorId AND e.id = :escritorId")
    boolean esFavorito(@Param("lectorId") Long lectorId, @Param("escritorId") Long escritorId);

    @Query("SELECT g.id FROM Lector l JOIN l.generosPreferidos g WHERE l.id = :lectorId")
    List<Long> idsGenerosPreferidos(@Param("lectorId") Long lectorId);

    @Query("SELECT e.id FROM Lector l JOIN l.escritoresFavoritos e WHERE l.id = :lectorId")
    List<Long> idsEscritoresFavoritos(@Param("lectorId") Long lectorId);

    @Query("SELECT p.id FROM Lector l JOIN l.publicacionesVistas p WHERE l.id = :lectorId")
    List<Long> idsPublicacionesVistas(@Param("lectorId") Long lectorId);

    long countByEstado(Estado estado);
}
