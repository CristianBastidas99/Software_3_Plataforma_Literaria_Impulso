package co.edu.uniquindio.proyecto.entidades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Comentario implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contenido;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaPublicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "escritor_id")
    private Escritor escritor;

    @ManyToOne
    @JoinColumn(name = "lector_id")
    private Lector lector;

    @ManyToOne
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;
}
