package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.GeneroServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio.Orden;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Página explorar: búsqueda de publicaciones (por frase, género y orden) o de escritores (por frase).
 * Al abrir la página se muestran todas las publicaciones, de la más reciente a la más antigua.
 */
@Component
@ViewScoped
@Getter
@Setter
public class ExplorarBean implements Serializable {

    private static final String PUBLICACIONES = "0";
    private static final String ESCRITORES = "1";

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private InteraccionServicio interaccionServicio;
    @Autowired
    private GeneroServicio generoServicio;

    private String searchType;
    private String searchInput;
    /** Género por el que filtrar las publicaciones; {@code null} para todos. */
    private Long idGenero;
    private String orden;

    private List<Genero> generos = new ArrayList<>();
    private List<Object> resultadosBusqueda;

    @PostConstruct
    public void inicializar() {
        searchType = PUBLICACIONES;
        orden = Orden.RECIENTES.name();
        resultadosBusqueda = new ArrayList<>();
        generos = new ArrayList<>(generoServicio.listarGeneros());
        generos.sort(Comparator.comparing(Genero::getNombre, String.CASE_INSENSITIVE_ORDER));
        resultadosBusqueda.addAll(interaccionServicio.buscarPublicaciones(null, null, Orden.RECIENTES));
    }

    public boolean isBuscandoPublicaciones() {
        return !ESCRITORES.equals(searchType);
    }

    public void realizarBusqueda() {
        resultadosBusqueda.clear();
        String frase = searchInput == null ? "" : searchInput.trim();
        if (!isBuscandoPublicaciones()) {
            if (frase.isEmpty()) {
                Mensajes.error("Escribe un nombre o una palabra para buscar escritores");
                return;
            }
            List<Escritor> escritores = escritorServicio.buscarEscritoresPorFrase(frase.toLowerCase());
            if (escritores.isEmpty()) {
                Mensajes.advertencia("No se encontraron escritores con esta búsqueda");
            }
            resultadosBusqueda.addAll(escritores);
            return;
        }
        List<Publicacion> publicaciones = interaccionServicio.buscarPublicaciones(frase, idGenero, ordenElegido());
        if (publicaciones.isEmpty()) {
            Mensajes.advertencia("No se encontraron publicaciones con estos filtros");
        }
        resultadosBusqueda.addAll(publicaciones);
    }

    /** Al cambiar entre publicaciones y escritores se limpian los resultados anteriores. */
    public void cambiarTipo() {
        resultadosBusqueda.clear();
        if (isBuscandoPublicaciones()) {
            realizarBusqueda();
        }
    }

    private Orden ordenElegido() {
        try {
            return Orden.valueOf(orden);
        } catch (RuntimeException e) {
            return Orden.RECIENTES;
        }
    }
}
