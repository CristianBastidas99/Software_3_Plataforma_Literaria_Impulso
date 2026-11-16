package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRecuperacionRepo extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Invalida los enlaces pendientes de un correo (al pedir uno nuevo o al cambiar la contraseña). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE LOWER(t.email) = LOWER(:email) AND t.usado = false")
    int invalidarTokensDe(@Param("email") String email);
}
