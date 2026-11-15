package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
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
import java.util.ArrayList;
import java.util.List;

@Component
@ViewScoped
@Getter
@Setter
public class PublicacionBean implements Serializable {

    private static final int ESCRITOR = 2;

    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private LoginBean loginBean;

    @Value("#{param['idProd']}")
    private String idProducto;

    private Publicacion publicacion;
    private List<Escritor> autores = new ArrayList<>();
    private List<Genero> generos = new ArrayList<>();
    /** El usuario con sesión es autor o coautor de la obra. */
    private boolean autor;

    /**
     * Indica si la publicación existe y el usuario puede verla; si es falso la vista muestra un mensaje
     * en lugar del detalle. Una obra retirada solo la ven sus autores.
     */
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
        } catch (Exception e) {
            publicacion = null;
            return;
        }
        if (publicacion.getObraLiteraria() != null) {
            Long idObra = publicacion.getObraLiteraria().getId();
            autores = publicacionServicio.obtenerEscritoresDeObra(idObra);
            generos = new ArrayList<>(publicacion.getObraLiteraria().getGeneros());
        }
        autor = esAutorConSesion();
        encontrada = isPublicada() || autor;
        if (!encontrada) {
            publicacion = null;
        }
    }

    public boolean isPublicada() {
        return publicacion != null && publicacion.getEstado() == Estado.PUBLICADO;
    }

    private boolean esAutorConSesion() {
        if (!Boolean.TRUE.equals(loginBean.getAutenticado()) || loginBean.getTipo() != ESCRITOR
                || loginBean.getUsuario() == null || loginBean.getUsuario().getId() == null) {
            return false;
        }
        Long idUsuario = loginBean.getUsuario().getId();
        return autores.stream().anyMatch(e -> idUsuario.equals(e.getId()));
    }
}
