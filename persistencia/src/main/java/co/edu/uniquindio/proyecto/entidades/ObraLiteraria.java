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
public class ObraLiteraria implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "titulo",  length = 70, nullable = false)
    private String titulo;

    @Temporal(TemporalType.DATE)
    private Date fechaPublicacion;

    @Column(name = "sinopsis",  length = 150, nullable = false)
    private String sinopsis;

    @Column(name = "isbn",  length = 100, nullable = false)
    private String isbn;

    @Column(name = "editorial",  length = 100, nullable = true)
    private String editorial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @OneToOne(mappedBy = "obraLiteraria", cascade = CascadeType.ALL)
    private Fragmento fragmento;

    @OneToOne(mappedBy = "obraLiteraria", cascade = CascadeType.ALL)
    private Publicacion publicacion;

    @ManyToMany
    @JoinTable(
            name = "escritor_obra_literaria",
            joinColumns = @JoinColumn(name = "obra_literaria_id"),
            inverseJoinColumns = @JoinColumn(name = "escritor_id")
    )
    private List<Escritor> escritores = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "obra_literaria_genero",
            joinColumns = @JoinColumn(name = "obra_literaria_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
    private List<Genero> generos = new java.util.ArrayList<>();

}
