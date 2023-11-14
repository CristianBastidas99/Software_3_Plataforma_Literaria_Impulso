package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExplorarBean implements Serializable{

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private PublicacionServicio publicacionServicio;

    private String searchType;
    private String searchInput;
    private List<Object> resultadosBusqueda;

    @PostConstruct
    public void inicializar(){
        resultadosBusqueda = new ArrayList<>();
    }

    public void realizarBusqueda() {

        resultadosBusqueda.clear();

        if(searchType.equals("1")){
            if(!searchInput.isEmpty()) {
                List<Escritor> escritores = escritorServicio.buscarEscritoresPorFrase(searchInput);
                if(escritores.isEmpty()){
                    mostrarMensaje("No se encontraron escritores con esta busqueda", FacesMessage.SEVERITY_ERROR);
                }
                resultadosBusqueda.addAll(escritores);
            }else{
                mostrarMensaje("El campo buscar esta vacio", FacesMessage.SEVERITY_ERROR);
            }
        }else if(searchType.equals("0")){
            if(!searchInput.isEmpty()) {
                List<Publicacion> publicaciones = publicacionServicio.buscarPublicacionPorFrase(searchInput);
                if(publicaciones.isEmpty()){
                    mostrarMensaje("No se encontraron publicaciones con esta busqueda", FacesMessage.SEVERITY_ERROR);
                }
                resultadosBusqueda.addAll((publicaciones));
            }else{
                mostrarMensaje("El campo buscar esta vacio", FacesMessage.SEVERITY_ERROR);
            }
        }
    }

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    private List<Escritor> escritoresEjemplo() {
        List<Escritor> escritorList = new ArrayList<>();

        for (int i = 1; i <= 9; i++) {
            Escritor escritor = new Escritor();
            escritor.setNombre("Escritor " + i);
            escritor.setEmail("escritor" + i + "@example.com");
            escritor.setPassword("password" + i);
            escritor.setUrlImagen("autor.jpg");
            escritor.setEstado(Estado.ACTIVO);  // Asigna el estado deseado
            escritor.setBiografia("Biografía del escritor" + i);


            // Puedes asignar valores específicos a la biografía, obrasLiterarias, comentarios, lectores, etc.

            escritorList.add(escritor);
        }

        return escritorList;
    }

    private List<Publicacion> publicacionesEjemplo() {
        List<Publicacion> publicacionList = new ArrayList<>();
        // Crear 9 publicaciones de ejemplo
        for (int i = 1; i <= 9; i++) {
            Publicacion publicacion = new Publicacion();
            publicacion.setContenido("Descripción breve del libro " + i);
            publicacion.setFechaPublicacion(new Date()); // Puedes ajustar la fecha según sea necesario
            publicacion.setTitulo("Título de la publicación " + i);
            publicacion.setUrlImagen("imagen.jpg");
            publicacion.setEstado(Estado.ACTIVO); // Puedes ajustar el estado según sea necesario

            // Configurar otros atributos según tus necesidades

            publicacionList.add(publicacion);
        }
        return  publicacionList;
    }

}
