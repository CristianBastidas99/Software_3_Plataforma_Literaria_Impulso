package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import co.edu.uniquindio.proyecto.dto.Estadisticas;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.AdministracionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de administración ({@code admin.xhtml}). Solo lo usa un administrador con sesión: la vista
 * redirige a los demás con {@link #verificarAcceso()} y cada acción vuelve a comprobarlo.
 */
@Component
@ViewScoped
@Getter
@Setter
public class AdminBean implements Serializable {

    private static final int ADMINISTRADOR = 1;
    private static final int MAX_MAS_VISTAS = 5;

    @Autowired
    private AdministracionServicio administracionServicio;
    @Autowired
    private ArchivoServicio archivoServicio;
    @Autowired
    private LoginBean loginBean;

    private Estadisticas estadisticas = new Estadisticas();
    private List<Escritor> escritores = new ArrayList<>();
    private List<Lector> lectores = new ArrayList<>();
    private List<Publicacion> publicaciones = new ArrayList<>();
    private List<Comentario> comentarios = new ArrayList<>();
    private List<Genero> generos = new ArrayList<>();
    private String nuevoGenero;

    @PostConstruct
    public void inicializar() {
        if (isAdministrador()) {
            cargarTodo();
        }
    }

    /** Acción de {@code f:viewAction}: manda al login o al inicio a quien no sea administrador. */
    public String verificarAcceso() {
        if (isAdministrador()) {
            return null;
        }
        if (!Boolean.TRUE.equals(loginBean.getAutenticado())) {
            Mensajes.advertencia("Inicia sesión como administrador para abrir el panel de control.");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "login?faces-redirect=true";
        }
        return "index?faces-redirect=true";
    }

    public boolean isAdministrador() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == ADMINISTRADOR;
    }

    // ---- Usuarios ----

    public void cambiarEstadoEscritor(Long id, boolean activo) {
        ejecutar(() -> administracionServicio.cambiarEstadoEscritor(id, activo),
                activo ? "El escritor se desbloqueó." : "El escritor se bloqueó: ya no puede iniciar sesión.");
    }

    public void cambiarEstadoLector(Long id, boolean activo) {
        ejecutar(() -> administracionServicio.cambiarEstadoLector(id, activo),
                activo ? "El lector se desbloqueó." : "El lector se bloqueó: ya no puede iniciar sesión.");
    }

    public boolean isActivo(Estado estado) {
        return estado == Estado.ACTIVO;
    }

    // ---- Publicaciones ----

    public void ocultarPublicacion(Long id) {
        ejecutar(() -> administracionServicio.ocultarPublicacion(id),
                "La publicación se ocultó: ya no la ven los lectores ni puede volver a publicarla su autor.");
    }

    public void mostrarPublicacion(Long id) {
        ejecutar(() -> administracionServicio.mostrarPublicacion(id), "La publicación vuelve a estar visible.");
    }

    public void eliminarPublicacion(Long id) {
        ejecutar(() -> administracionServicio.eliminarPublicacion(id).forEach(archivoServicio::eliminar),
                "La publicación y su obra se eliminaron.");
    }

    public String textoEstadoPublicacion(Estado estado) {
        if (estado == null) {
            return "Sin estado";
        }
        switch (estado) {
            case PUBLICADO:
                return "Publicada";
            case INACTIVO:
                return "Retirada por el autor";
            case RECHAZADO:
                return "Oculta";
            case PENDIENTE:
                return "Pendiente";
            default:
                return estado.name();
        }
    }

    public String severidad(Estado estado) {
        if (estado == Estado.PUBLICADO || estado == Estado.ACTIVO || estado == Estado.APROBADO) {
            return "success";
        }
        if (estado == Estado.RECHAZADO || estado == Estado.INACTIVO) {
            return "danger";
        }
        return "warning";
    }

    // ---- Comentarios ----

    public void ocultarComentario(Long id) {
        ejecutar(() -> administracionServicio.ocultarComentario(id), "El comentario se ocultó.");
    }

    public void mostrarComentario(Long id) {
        ejecutar(() -> administracionServicio.mostrarComentario(id), "El comentario vuelve a estar visible.");
    }

    public void eliminarComentario(Long id) {
        ejecutar(() -> administracionServicio.eliminarComentario(id), "El comentario se eliminó.");
    }

    public String autorComentario(Comentario c) {
        if (c.getLector() != null) {
            return c.getLector().getNombre() + " (lector)";
        }
        return c.getEscritor() != null ? c.getEscritor().getNombre() + " (escritor)" : "Anónimo";
    }

    // ---- Géneros ----

    public void crearGenero() {
        ejecutar(() -> administracionServicio.crearGenero(nuevoGenero), "Género creado.");
        if (!FacesContext.getCurrentInstance().isValidationFailed()) {
            nuevoGenero = "";
        }
    }

    public void renombrarGenero(Genero genero) {
        ejecutar(() -> administracionServicio.renombrarGenero(genero.getId(), genero.getNombre()), "Género actualizado.");
    }

    public void eliminarGenero(Long id) {
        ejecutar(() -> administracionServicio.eliminarGenero(id), "Género eliminado.");
    }

    public long obrasDeGenero(Long id) {
        return administracionServicio.contarObrasDeGenero(id);
    }

    // ---------------------------------------------------------------------------------------------

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    /** Comprueba el permiso, ejecuta la acción, muestra el resultado y recarga los datos. */
    private void ejecutar(Accion accion, String mensajeExito) {
        if (!isAdministrador()) {
            Mensajes.error("Solo el administrador puede hacer esta acción.");
            return;
        }
        try {
            accion.ejecutar();
            Mensajes.info(mensajeExito);
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
        cargarTodo();
        // Las tablas guardan el valor escrito en cada campo según la posición de la fila; si la lista
        // cambia (un género nuevo o eliminado desplaza las filas) ese valor acabaría en otra fila.
        PrimeFaces.current().resetInputs("formAdmin");
    }

    private void cargarTodo() {
        estadisticas = administracionServicio.obtenerEstadisticas(MAX_MAS_VISTAS);
        escritores = administracionServicio.listarEscritores();
        lectores = administracionServicio.listarLectores();
        publicaciones = administracionServicio.listarPublicaciones();
        comentarios = administracionServicio.listarComentarios();
        generos = administracionServicio.listarGeneros();
    }
}
