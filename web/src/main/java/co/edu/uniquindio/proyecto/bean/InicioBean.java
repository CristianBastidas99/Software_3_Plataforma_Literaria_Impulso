package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@ViewScoped
@Getter
@Setter
public class InicioBean implements Serializable {

    private String mensaje = "Mi primera página en JSF";
    private String nameAccount = "Cristian Fabian";
    private List<Publicacion> publicacions;
    private List<Escritor> escritors;
    private List<ResponsiveOption> responsiveOptions;
    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private  EscritorServicio escritorServicio;

    @PostConstruct
    public void inicializar(){
        publicacions = publicacionServicio.listarPublicaciones();
        if(publicacions.isEmpty()){
            publicacions = publicacionesEjemplo();
        }

        escritors = escritorServicio.listarEscritores();
        if(escritors.isEmpty()){
            escritors = escritoresEjemplo();
        }

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));
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

    public String registro() {
        return "registro?faces-redirect=true";
    }

}
