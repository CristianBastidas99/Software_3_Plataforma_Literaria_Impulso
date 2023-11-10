package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FragmentoRepo extends JpaRepository<Fragmento, Long> {
    @Query("SELECT f FROM Fragmento f WHERE f.estado = 'APROBADO' AND f.obraLiteraria.id = :obraId")
    List<Fragmento> recuperarFragmentosAprobadosDeObra(@Param("obraId") Long obraId);
    @Query("SELECT f FROM Fragmento f WHERE f.estado = 'PENDIENTE'")
    List<Fragmento> buscarFragmentosPendientes();
    @Query("SELECT f FROM Fragmento f JOIN f.obraLiteraria o WHERE o.estado = 'PUBLICADO'")
    List<Fragmento> listarFragmentosDeObrasPublicadas();
    @Query("SELECT f FROM Fragmento f WHERE f.estado = :estado ORDER BY f.id")
    List<Fragmento> obtenerFragmentosPorEstado(@Param("estado") String estado);
    @Query("SELECT COUNT(f) FROM Fragmento f WHERE f.estado = 'APROBADO'")
    Long contarFragmentosAprobados();
    @Query("SELECT f FROM Fragmento f WHERE LOWER(f.url) LIKE LOWER(:palabraClave)")
    List<Fragmento> buscarFragmentosPorContenido(@Param("palabraClave") String palabraClave);
    @Query("SELECT f FROM Fragmento f WHERE f.estado = 'PENDIENTE' ORDER BY f.id")
    List<Fragmento> listarFragmentosPendientesOrdenadosPorFechaCreacion();
    @Query("SELECT f FROM Fragmento f WHERE f.obraLiteraria.id = :obraId ORDER BY f.estado")
    List<Fragmento> obtenerFragmentosDeObraOrdenadosPorEstado(@Param("obraId") Long obraId);

}
