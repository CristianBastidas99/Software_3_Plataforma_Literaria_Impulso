package co.edu.uniquindio.proyecto.entidades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Publicacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date fechaPublicacion;

    @Column(name = "titulo",  length = 70, nullable = false)
    private String titulo;

    @Column(name = "urlImagen", nullable = false)
    private String urlImagen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @OneToOne
    @JoinColumn(name = "obra_literaria_id")
    private ObraLiteraria obraLiteraria;

    @OneToMany(mappedBy = "publicacion")
    private List<Comentario> comentarios;
}
