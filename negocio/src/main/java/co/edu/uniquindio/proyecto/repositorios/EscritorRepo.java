package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscritorRepo extends JpaRepository<Escritor, Long> {

    @Query("SELECT e FROM Escritor e WHERE e.email = :correoElectronico AND e.password = :contrasena")
    Optional<Escritor> iniciarSesionEscritor(@Param("correoElectronico") String correoElectronico, @Param("contrasena") String contrasena);
    @Query("SELECT e FROM Escritor e WHERE e.email = :correoElectronico")
    Optional<Escritor> buscarEscritorPorCorreo(@Param("correoElectronico") String correoElectronico);
    @Query("SELECT e FROM Escritor e WHERE e.estado = 'ACTIVO' AND (LOWER(e.nombre) LIKE %:frase% OR LOWER(e.biografia) LIKE %:frase%)")
    List<Escritor> obtenerEscritoresPorFrase(@Param("frase") String frase);
    @Query("SELECT DISTINCT p FROM Publicacion p JOIN p.obraLiteraria o JOIN o.escritores e WHERE e.id = :escritorId AND p.estado = 'PUBLICADO'")
    List<Publicacion> obtenerPublicacionesAprobadasDeEscritor(@Param("escritorId") Long escritorId);


    @Query("SELECT e FROM Escritor e WHERE e.estado = 'ACTIVO'")
    List<Escritor> obtenerEscritoresActivos();
    @Query("SELECT DISTINCT e FROM Escritor e JOIN e.obrasLiterarias o JOIN o.generos g WHERE g.id = :generoId")
    List<Escritor> buscarEscritoresPorGenero(@Param("generoId") Long generoId);
    @Query("SELECT COUNT(e) FROM Escritor e")
    Long contarEscritores();
    @Query("SELECT e FROM Escritor e WHERE LOWER(e.nombre) LIKE LOWER(:nombre)")
    List<Escritor> buscarEscritoresPorNombre(@Param("nombre") String nombre);
    @Query("SELECT e FROM Escritor e WHERE e.estado = :estado ORDER BY e.id")
    List<Escritor> obtenerEscritoresPorEstado(@Param("estado") String estado);
    @Query("SELECT DISTINCT e FROM Escritor e JOIN e.obrasLiterarias o WHERE o.estado = 'PENDIENTE'")
    List<Escritor> listarEscritoresConObrasPendientes();
}
