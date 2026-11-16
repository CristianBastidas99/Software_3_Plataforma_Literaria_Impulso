package co.edu.uniquindio.proyecto.entidades;

import co.edu.uniquindio.proyecto.seguridad.CifradoContrasenaListener;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(CifradoContrasenaListener.class)
@ToString
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Usuario implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 30, nullable = false)
    private String nombre;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    /** Hash BCrypt (60 caracteres); nunca la contraseña en claro. No se incluye en {@code toString}. */
    @Column(name = "password", length = 100, nullable = false)
    @ToString.Exclude
    private String password;

    @Column(name = "urlImagen", nullable = false)
    private String urlImagen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Usuario usuario = (Usuario) o;
        return getId() != null && Objects.equals(getId(), usuario.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
