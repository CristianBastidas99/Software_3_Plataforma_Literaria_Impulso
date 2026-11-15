package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
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
public class ExplorarBean implements Serializable {

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private PublicacionServicio publicacionServicio;

    private String searchType;
    private String searchInput;
    private List<Object> resultadosBusqueda;

    @PostConstruct
    public void inicializar() {
        searchType = "0";
        resultadosBusqueda = new ArrayList<>();
    }

    public void realizarBusqueda() {
        resultadosBusqueda.clear();

        if (searchInput == null || searchInput.isBlank()) {
            Mensajes.error("El campo de búsqueda está vacío");
            return;
        }
        String frase = searchInput.trim();

        if ("1".equals(searchType)) {
            List<Escritor> escritores = escritorServicio.buscarEscritoresPorFrase(frase);
            if (escritores.isEmpty()) {
                Mensajes.advertencia("No se encontraron escritores con esta búsqueda");
            }
            resultadosBusqueda.addAll(escritores);
        } else {
            List<Publicacion> publicaciones = publicacionServicio.buscarPublicacionPorFrase(frase);
            if (publicaciones.isEmpty()) {
                Mensajes.advertencia("No se encontraron publicaciones con esta búsqueda");
            }
            resultadosBusqueda.addAll(publicaciones);
        }
    }
}
