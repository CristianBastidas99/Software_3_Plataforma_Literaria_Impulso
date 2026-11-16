package co.edu.uniquindio.proyecto.seguridad;

import co.edu.uniquindio.proyecto.entidades.Usuario;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * Cifra la contraseña de un usuario justo antes de guardarlo si todavía está en claro. Así no se
 * guarda texto plano por ningún camino: registro, cambio o recuperación de contraseña, datos de
 * demostración o cualquier llamada directa a un repositorio. Un valor ya cifrado no se toca.
 */
public class CifradoContrasenaListener {

    @PrePersist
    @PreUpdate
    public void cifrar(Usuario usuario) {
        String password = usuario.getPassword();
        if (password != null && !Contrasenas.estaCifrada(password)) {
            usuario.setPassword(Contrasenas.cifrar(password));
        }
    }
}
