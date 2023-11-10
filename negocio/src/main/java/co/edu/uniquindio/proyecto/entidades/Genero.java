package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Genero implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToMany(mappedBy = "generos")
    @ToString.Exclude
    private List<ObraLiteraria> obraLiterarias = new ArrayList<>();

    @ManyToMany(mappedBy = "generosPreferidos")
    @ToString.Exclude
    private List<Lector> lectores = new ArrayList<>();
}