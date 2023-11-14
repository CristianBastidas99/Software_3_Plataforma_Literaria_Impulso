package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@ViewScoped
@Getter
@Setter
public class EscritorBean implements Serializable{

    @Autowired
    private  EscritorServicio escritorServicio;
    @Value("#{param['idEscr']}")
    private String idEscritor;

    private List<ResponsiveOption> responsiveOptions;
    private Escritor escritor;
    private List<Publicacion> publicacionList;

    @PostConstruct
    public void inicializar(){

        escritor = new Escritor();
        publicacionList = new ArrayList<>();

        if(idEscritor!=null && !idEscritor.equals("")){
            try {
                Long id = Long.valueOf(idEscritor);
                System.out.println(id);
                escritor = escritorServicio.obtenerEscritor(id);
                publicacionList = escritorServicio.obtenerPublicacionesDeEscritor(id);
            } catch (Exception e) {
                mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
            }
        }

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));
    }

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }


}
