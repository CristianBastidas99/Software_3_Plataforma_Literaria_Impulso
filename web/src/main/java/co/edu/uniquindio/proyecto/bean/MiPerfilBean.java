package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.GeneroServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Página "Mi perfil" del lector autenticado: géneros preferidos, escritores favoritos,
 * publicaciones recomendadas e historial de publicaciones vistas.
 */
@Component
@ViewScoped
@Getter
public class MiPerfilBean implements Serializable {

    private static final int LECTOR = 3;

    @Autowired
    private LectorServicio lectorServicio;
    @Autowired
    private GeneroServicio generoServicio;
    @Autowired
    private InteraccionServicio interaccionServicio;
    @Autowired
    private LoginBean loginBean;

    /** Géneros marcados en el selector de preferencias. */
    @Setter
    private List<Long> idsGenerosSeleccionados = new ArrayList<>();
    private List<Genero> generosDisponibles = new ArrayList<>();

    private Lector lector;
    private List<Genero> generosPreferidos = new ArrayList<>();
    private List<Escritor> escritoresFavoritos = new ArrayList<>();
    private List<Publicacion> publicacionesRecomendadas = new ArrayList<>();
    private List<Publicacion> publicacionesVistas = new ArrayList<>();

    @PostConstruct
    public void inicializar() {
        if (!isEsLector()) {
            return;
        }
        try {
            lector = lectorServicio.obtenerLector(loginBean.getUsuario().getId());
            // Se copian las colecciones perezosas mientras la sesión de JPA de la petición sigue abierta
            generosPreferidos = copiar(lector.getGenerosPreferidos());
            escritoresFavoritos = copiar(lector.getEscritoresFavoritos());
            publicacionesRecomendadas = publicadas(lector.getPublicacionesRecomendadas());
            publicacionesVistas = publicadas(lector.getPublicacionesVistas());
            generosPreferidos.sort(Comparator.comparing(Genero::getNombre, String.CASE_INSENSITIVE_ORDER));
            idsGenerosSeleccionados = generosPreferidos.stream().map(Genero::getId).collect(Collectors.toList());
            generosDisponibles = new ArrayList<>(generoServicio.listarGeneros());
            generosDisponibles.sort(Comparator.comparing(Genero::getNombre, String.CASE_INSENSITIVE_ORDER));
        } catch (Exception e) {
            lector = null;
        }
    }

    public void guardarGeneros() {
        if (!isEsLector() || lector == null) {
            Mensajes.error("Inicia sesión como lector para elegir tus géneros preferidos");
            return;
        }
        try {
            interaccionServicio.actualizarGenerosPreferidos(lector.getId(), idsGenerosSeleccionados);
            generosPreferidos = generosDisponibles.stream()
                    .filter(g -> idsGenerosSeleccionados.contains(g.getId()))
                    .collect(Collectors.toList());
            Mensajes.info(generosPreferidos.isEmpty()
                    ? "Quitaste tus géneros preferidos."
                    : "Guardamos tus géneros preferidos. Verás recomendaciones en la página de inicio.");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
    }

    /** La página solo tiene sentido para un lector con sesión iniciada. */
    public boolean isEsLector() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == LECTOR
                && loginBean.getUsuario() != null && loginBean.getUsuario().getId() != null;
    }

    /** Las obras retiradas por su autor no se muestran. */
    private static List<Publicacion> publicadas(Collection<Publicacion> coleccion) {
        List<Publicacion> lista = copiar(coleccion);
        lista.removeIf(p -> p.getEstado() != Estado.PUBLICADO);
        return lista;
    }

    private static <T> List<T> copiar(Collection<T> coleccion) {
        return coleccion == null ? new ArrayList<>() : new ArrayList<>(coleccion);
    }
}
