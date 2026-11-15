package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
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

    @PostConstruct
    public void inicializar() {
        publicacions = publicacionServicio.listarPublicacionesPublicadas();
        escritors = escritorServicio.listarEscritores();

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));
    }

    public String registro() {
        return "registro?faces-redirect=true";
    }

    public String login() {
        return "login?faces-redirect=true";
    }
}
