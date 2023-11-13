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
import javax.faces.annotation.ManagedProperty;
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
public class PublicacionBean implements Serializable{

    @Autowired
    private PublicacionServicio publicacionServicio;

    @Value("#{param['idProd']}")
    private String idProducto;

    private Publicacion publicacion;
    private Escritor escritor;

    @PostConstruct
    public void inicializar(){
        publicacion = new Publicacion();
        escritor = new Escritor();
        if(idProducto!=null){
            try {
                publicacion = publicacionServicio.obtenerPublicacion(Long.valueOf(idProducto));
                escritor = publicacionServicio.obtenerEscritorDePublicacion(publicacion.getObraLiteraria().getId());
            } catch (Exception e) {
                mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
            }
        }else{
            System.out.println("No LLego");
        }
    }

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

}
