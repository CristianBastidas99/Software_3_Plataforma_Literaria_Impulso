package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraLiterariaRepo extends JpaRepository<ObraLiteraria, Long> {
    @Query("SELECT o FROM ObraLiteraria o JOIN o.generos g WHERE g.id = :generoId")
    List<ObraLiteraria> recuperarObrasPorGenero(@Param("generoId") Long generoId);
    @Query("SELECT o FROM ObraLiteraria o WHERE o.estado = :estado")
    List<ObraLiteraria> buscarObrasPorEstado(@Param("estado") String estado);
    @Query("SELECT o FROM ObraLiteraria o WHERE o.estado = 'PUBLICADO' ORDER BY o.fechaPublicacion DESC")
    List<ObraLiteraria> listarObrasPublicadasOrdenadasPorFecha();
    @Query("SELECT DISTINCT o FROM ObraLiteraria o JOIN o.fragmento f WHERE f.estado = 'PENDIENTE'")
    List<ObraLiteraria> obtenerObrasConFragmentosPendientes();
    @Query("SELECT COUNT(o) FROM ObraLiteraria o WHERE o.estado = 'APROBADO'")
    Long contarObrasAprobadas();
    @Query("SELECT o FROM ObraLiteraria o WHERE LOWER(o.titulo) LIKE LOWER(:palabraClave)")
    List<ObraLiteraria> buscarObrasPorTitulo(@Param("palabraClave") String palabraClave);
    @Query("SELECT DISTINCT o FROM ObraLiteraria o JOIN o.escritores e WHERE e.estado = 'ACTIVO'")
    List<ObraLiteraria> listarObrasConEscritoresActivos();
    @Query("SELECT o FROM ObraLiteraria o WHERE (SELECT COUNT(f) FROM o.fragmento f WHERE f.estado = 'APROBADO') > :numeroFragmentos")
    List<ObraLiteraria> obtenerObrasConMasFragmentosAprobados(@Param("numeroFragmentos") Long numeroFragmentos);

}
