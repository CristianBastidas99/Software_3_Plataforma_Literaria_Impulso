package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

@Component
@ViewScoped
@Getter
@Setter
public class PublicacionBean implements Serializable {

    @Autowired
    private PublicacionServicio publicacionServicio;

    @Value("#{param['idProd']}")
    private String idProducto;

    private Publicacion publicacion;
    private Escritor escritor;
    /** Indica si la publicación solicitada existe; si es falso la vista muestra un mensaje en lugar del detalle. */
    private boolean encontrada;

    @PostConstruct
    public void inicializar() {
        encontrada = false;

        Long id = ParametrosUrl.aLong(idProducto);
        if (id == null) {
            return;
        }
        try {
            publicacion = publicacionServicio.obtenerPublicacion(id);
            encontrada = true;
        } catch (Exception e) {
            publicacion = null;
            return;
        }

        if (publicacion.getObraLiteraria() != null) {
            try {
                escritor = publicacionServicio.obtenerEscritorDePublicacion(publicacion.getObraLiteraria().getId());
            } catch (Exception e) {
                escritor = null;
            }
        }
    }
}
