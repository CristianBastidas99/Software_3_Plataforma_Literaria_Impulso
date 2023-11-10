package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
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
    @ToString.Exclude
    private ObraLiteraria obraLiteraria;

    @ToString.Include
    public String idObraLiteraria(){return obraLiteraria.getId()+"";}

    @OneToMany(mappedBy = "publicacion")
    @ToString.Exclude
    private List<Comentario> comentarios;

    @ManyToMany(mappedBy = "publicacionesVistas")
    @ToString.Exclude
    private List<Lector> lectoresVisitas = new ArrayList<>();

    @ManyToMany(mappedBy = "publicacionesRecomendadas")
    @ToString.Exclude
    private List<Lector> lectoresRecomendaciones = new ArrayList<>();
}
