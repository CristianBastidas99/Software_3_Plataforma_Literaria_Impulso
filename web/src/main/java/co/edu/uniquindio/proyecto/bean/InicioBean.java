package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
    @Value(value = "#{loginBean.usuario}")
    private Usuario usuarioSesion;

    @PostConstruct
    public void inicializar() {
        publicacions = publicacionServicio.listarPublicaciones();
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

    public String cuenta() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + "/editar_perfil.xhtml?idUsu=" + usuarioSesion.getId());
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        return null;
    }
}
