package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministradorRepo extends JpaRepository<Administrador, Long> {

    @Query("SELECT a FROM Administrador a WHERE a.estado = 'ACTIVO'")
    List<Administrador> obtenerAdministradoresActivos();

    @Query("SELECT a FROM Administrador a WHERE a.email = :correoElectronico")
    Administrador buscarAdministradorPorCorreo(@Param("correoElectronico") String correoElectronico);

    @Query("SELECT COUNT(a) FROM Administrador a")
    Long contarAdministradores();

    @Query("SELECT a FROM Administrador a ORDER BY a.nombre")
    List<Administrador> listarAdministradoresOrdenadosPorNombre();

    @Query("SELECT a FROM Administrador a WHERE a.estado = :estado")
    List<Administrador> obtenerAdministradoresPorEstado(@Param("estado") String estado);

    @Query("SELECT a FROM Administrador a WHERE LOWER(a.nombre) LIKE LOWER(:nombre)")
    List<Administrador> buscarAdministradoresPorNombre(@Param("nombre") String nombre);

    @Query("SELECT a FROM Administrador a ORDER BY a.id")
    List<Administrador> obtenerAdministradoresOrdenadosPorFechaCreacion();

    @Query("SELECT a FROM Administrador a WHERE a.estado = 'INACTIVO'")
    List<Administrador> listarAdministradoresInactivos();


}
