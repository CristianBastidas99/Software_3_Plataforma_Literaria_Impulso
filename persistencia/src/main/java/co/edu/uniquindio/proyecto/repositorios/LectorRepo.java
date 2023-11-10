package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectorRepo extends JpaRepository<Lector, Long> {
    @Query("SELECT l FROM Lector l WHERE l.estado = 'ACTIVO'")
    List<Lector> obtenerLectoresActivos();
    @Query("SELECT l FROM Lector l WHERE l.email = :correoElectronico")
    Lector buscarLectorPorCorreo(@Param("correoElectronico") String correoElectronico);
    @Query("SELECT l FROM Lector l ORDER BY l.nombre")
    List<Lector> listarLectoresOrdenadosPorNombre();
    //@Query("SELECT DISTINCT l FROM Lector l JOIN l.lectorGeneroPreferido lgp JOIN lgp.genero g JOIN g.obrasLiterarias o WHERE l.estado = 'INACTIVO'")
    //List<Lector> obtenerLectoresInactivosConObrasFavoritas();
    @Query("SELECT COUNT(l) FROM Lector l")
    Long contarLectores();
    @Query("SELECT l FROM Lector l WHERE LOWER(l.nombre) LIKE LOWER(:nombre)")
    List<Lector> buscarLectoresPorNombre(@Param("nombre") String nombre);
    @Query("SELECT l FROM Lector l WHERE l.estado = :estado ORDER BY l.id")
    List<Lector> obtenerLectoresPorEstado(@Param("estado") String estado);
    //@Query("SELECT DISTINCT l FROM Lector l JOIN l.lectorPublicacionRecomendada lpr JOIN lpr.publicacion p WHERE p.estado = 'VISTO'")
    //List<Lector> listarLectoresConPublicacionesRecomendadasVistas();

}
