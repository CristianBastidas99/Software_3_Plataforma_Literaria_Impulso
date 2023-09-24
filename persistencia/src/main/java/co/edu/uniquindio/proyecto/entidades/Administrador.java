package co.edu.uniquindio.proyecto.entidades;

import javax.persistence.Entity;
import java.io.Serializable;
import lombok.*;

@Entity
@Getter
@Setter
//@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Administrador extends Usuario implements Serializable {


}
