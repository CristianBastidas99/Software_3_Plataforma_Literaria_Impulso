package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

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
@ToString
public class ObraLiteraria implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "titulo",  length = 70, nullable = false)
    private String titulo;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
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
    @ToString.Exclude
    private Fragmento fragmento;

    @ToString.Include
    public String idFragmento(){return fragmento.getId()+"";}

    @OneToOne(mappedBy = "obraLiteraria", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Publicacion publicacion;

    @ToString.Include
    public String idPublicacion(){return publicacion.getId()+"";}

    @ManyToMany
    @JoinTable(
            name = "escritor_obra_literaria",
            joinColumns = @JoinColumn(name = "obra_literaria_id"),
            inverseJoinColumns = @JoinColumn(name = "escritor_id")
    )
    @ToString.Exclude
    private List<Escritor> escritores = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "obra_literaria_genero",
            joinColumns = @JoinColumn(name = "obra_literaria_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
    @ToString.Exclude
    private List<Genero> generos = new ArrayList<>();

}
