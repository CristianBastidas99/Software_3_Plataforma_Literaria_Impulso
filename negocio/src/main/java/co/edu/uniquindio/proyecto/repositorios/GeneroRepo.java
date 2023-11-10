package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneroRepo extends JpaRepository<Genero, Long> {
    @Query("SELECT g FROM Genero g ORDER BY g.nombre")
    List<Genero> listarGenerosOrdenadosAlfabeticamente();
    @Query("SELECT g FROM Genero g WHERE LOWER(g.nombre) LIKE LOWER(:palabraClave)")
    List<Genero> buscarGenerosPorNombre(@Param("palabraClave") String palabraClave);
    @Query("SELECT g, COUNT(o) AS numObras FROM Genero g JOIN g.obraLiterarias o GROUP BY g ORDER BY numObras DESC")
    List<Object[]> contarObrasPorGenero();
    @Query("SELECT DISTINCT g FROM Genero g JOIN g.obraLiterarias o WHERE o.estado = 'PUBLICADO'")
    List<Genero> obtenerGenerosConObrasPublicadas();
    @Query("SELECT g FROM Genero g WHERE g.id = :generoId")
    Genero buscarGeneroPorId(@Param("generoId") Long generoId);
    @Query("SELECT DISTINCT g FROM Genero g JOIN g.obraLiterarias o WHERE o.estado = 'PENDIENTE'")
    List<Genero> listarGenerosConObrasPendientes();
    @Query("SELECT g FROM Genero g JOIN g.obraLiterarias o JOIN o.escritores e")
    List<Genero> recuperarGenerosConEscritoresAsociados();
    @Query("SELECT g FROM Genero g WHERE g.obraLiterarias IS EMPTY")
    List<Genero> obtenerGenerosSinObrasAsociadas();

}
