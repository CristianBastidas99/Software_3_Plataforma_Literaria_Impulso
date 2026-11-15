package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private LoginBean loginBean;

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
        } catch (Exception e) {
            lector = null;
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
