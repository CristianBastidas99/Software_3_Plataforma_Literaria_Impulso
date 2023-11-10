package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscritorRepo extends JpaRepository<Escritor, Long> {

    @Query("SELECT e FROM Escritor e WHERE e.estado = 'ACTIVO'")
    List<Escritor> obtenerEscritoresActivos();
    @Query("SELECT DISTINCT e FROM Escritor e JOIN e.obrasLiterarias o JOIN o.generos g WHERE g.id = :generoId")
    List<Escritor> buscarEscritoresPorGenero(@Param("generoId") Long generoId);
    @Query("SELECT e, COUNT(o) AS numObras FROM Escritor e JOIN e.obrasLiterarias o WHERE o.estado = 'PUBLICADO' GROUP BY e ORDER BY numObras DESC")
    List<Object[]> listarEscritoresPorNumObrasPublicadas();
    @Query("SELECT e FROM Escritor e WHERE e.estado = 'INACTIVO' AND LOWER(e.biografia) LIKE LOWER(:palabraClave)")
    List<Escritor> obtenerEscritoresInactivosConBiografia(@Param("palabraClave") String palabraClave);
    @Query("SELECT COUNT(e) FROM Escritor e")
    Long contarEscritores();
    @Query("SELECT e FROM Escritor e WHERE LOWER(e.nombre) LIKE LOWER(:nombre)")
    List<Escritor> buscarEscritoresPorNombre(@Param("nombre") String nombre);
    @Query("SELECT e FROM Escritor e WHERE e.estado = :estado ORDER BY e.id")
    List<Escritor> obtenerEscritoresPorEstado(@Param("estado") String estado);
    @Query("SELECT DISTINCT e FROM Escritor e JOIN e.obrasLiterarias o WHERE o.estado = 'PENDIENTE'")
    List<Escritor> listarEscritoresConObrasPendientes();


}
