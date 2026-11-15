package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
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
public class InicioBean implements Serializable {

    private List<Publicacion> publicacions;
    private List<Escritor> escritors;
    private List<ResponsiveOption> responsiveOptions;
    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private InteraccionServicio interaccionServicio;
    @Autowired
    private LoginBean loginBean;

    private static final int LECTOR = 3;
    private static final int MAX_RECOMENDADAS = 8;

    /** Publicaciones de los géneros preferidos y escritores favoritos del lector con sesión. */
    private List<Publicacion> recomendadas = new ArrayList<>();

    @PostConstruct
    public void inicializar() {
        publicacions = publicacionServicio.listarPublicacionesPublicadas();
        escritors = escritorServicio.listarEscritores();
        if (isLector()) {
            recomendadas = interaccionServicio.recomendadasPara(loginBean.getUsuario().getId(), MAX_RECOMENDADAS);
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

    public String registro() {
        return "registro?faces-redirect=true";
    }

    public String login() {
        return "login?faces-redirect=true";
    }
}
