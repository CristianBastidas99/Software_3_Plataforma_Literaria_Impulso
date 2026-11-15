package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
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
public class EscritorBean implements Serializable {

    private static final int LECTOR = 3;

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private InteraccionServicio interaccionServicio;
    @Autowired
    private LoginBean loginBean;

    private long seguidores;
    private boolean favorito;
    @Value("#{param['idEscr']}")
    private String idEscritor;

    private List<ResponsiveOption> responsiveOptions;
    private Escritor escritor;
    private List<Publicacion> publicacionList;
    /** Indica si el escritor solicitado existe; si es falso la vista muestra un mensaje en lugar del perfil. */
    private boolean encontrado;

    @PostConstruct
    public void inicializar() {
        publicacionList = new ArrayList<>();
        encontrado = false;

        Long id = ParametrosUrl.aLong(idEscritor);
        if (id != null) {
            try {
                escritor = escritorServicio.obtenerEscritor(id);
                publicacionList = escritorServicio.obtenerPublicacionesDeEscritor(id);
                encontrado = true;
                cargarFavorito();
            } catch (Exception e) {
                escritor = null;
            }
        }

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));
    }

    public boolean isLector() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == LECTOR
                && loginBean.getUsuario() != null && loginBean.getUsuario().getId() != null;
    }

    public void alternarFavorito() {
        if (!isLector() || escritor == null) {
            Mensajes.error("Inicia sesión como lector para seguir a un escritor");
            return;
        }
        try {
            boolean activo = interaccionServicio.alternarFavorito(loginBean.getUsuario().getId(), escritor.getId());
            Mensajes.info(activo ? "Ahora sigues a " + escritor.getNombre() + "." : "Dejaste de seguir a " + escritor.getNombre() + ".");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        cargarFavorito();
    }

    private void cargarFavorito() {
        seguidores = interaccionServicio.contarSeguidores(escritor.getId());
        favorito = isLector() && interaccionServicio.esFavorito(loginBean.getUsuario().getId(), escritor.getId());
    }
}
