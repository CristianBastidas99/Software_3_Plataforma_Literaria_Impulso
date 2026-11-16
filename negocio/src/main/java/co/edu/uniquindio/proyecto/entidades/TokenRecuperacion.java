package co.edu.uniquindio.proyecto.entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Enlace de "¿Olvidaste tu contraseña?". Solo se guarda el hash SHA-256 del token: quien lea la
 * base de datos no puede usarlo para cambiar contraseñas. El correo identifica al usuario porque
 * es único entre administradores, escritores y lectores.
 */
@Entity
@Table(name = "token_recuperacion")
@Getter
@Setter
@NoArgsConstructor
public class TokenRecuperacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /** Se marca al usarlo o al pedir uno nuevo: un enlace solo sirve una vez. */
    @Column(name = "usado", nullable = false)
    private boolean usado;

    public boolean isVigente(LocalDateTime ahora) {
        return !usado && ahora.isBefore(fechaExpiracion);
    }
}
