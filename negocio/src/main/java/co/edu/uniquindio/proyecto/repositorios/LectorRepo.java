package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
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
}
