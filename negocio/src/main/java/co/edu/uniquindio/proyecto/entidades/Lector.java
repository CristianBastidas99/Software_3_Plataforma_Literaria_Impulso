package co.edu.uniquindio.proyecto.entidades;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Lector extends Usuario implements Serializable{

    @OneToMany(mappedBy = "lector")
    private List<Comentario> comentarios;

    @ManyToMany
    @JoinTable(
            name = "lector_escritor_favorito",
            joinColumns = @JoinColumn(name = "lector_id"),
            inverseJoinColumns = @JoinColumn(name = "escritor_id")
    )
    private List<Escritor> escritoresFavoritos;

    @ManyToMany
    @JoinTable(
            name = "lector_publicacion_recomendada",
            joinColumns = @JoinColumn(name = "lector_id"),
            inverseJoinColumns = @JoinColumn(name = "publicacion_id")
    )
    private List<Publicacion> publicacionesRecomendadas;

    @ManyToMany
    @JoinTable(
            name = "lector_publicacion_vista",
            joinColumns = @JoinColumn(name = "lector_id"),
            inverseJoinColumns = @JoinColumn(name = "publicacion_id")
    )
    private List<Publicacion> publicacionesVistas;

    @ManyToMany
    @JoinTable(
            name = "lector_genero_preferido",
            joinColumns = @JoinColumn(name = "lector_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private List<Genero> generosPreferidos;
}

