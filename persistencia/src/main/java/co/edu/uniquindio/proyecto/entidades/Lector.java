package co.edu.uniquindio.proyecto.entidades;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
//@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Lector extends Usuario implements Serializable{


}

