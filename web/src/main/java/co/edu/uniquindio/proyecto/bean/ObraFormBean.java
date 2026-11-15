package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.archivos.ArchivoInvalidoException;
import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import co.edu.uniquindio.proyecto.dto.DatosObra;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.GeneroServicio;
import co.edu.uniquindio.proyecto.servicios.GestionObraServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formulario para crear una obra ({@code nueva_obra.xhtml}) o editar una existente
 * ({@code nueva_obra.xhtml?idObra=N}). Solo lo usan escritores con sesión iniciada, y en modo
 * edición solo si son autores de la obra; el servicio vuelve a comprobarlo al guardar.
 *
 * <p>La portada y el PDF se suben al elegirlos y quedan pendientes hasta guardar. Si el escritor
 * reemplaza un archivo o abandona el formulario, los archivos que no llegaron a usarse se borran.</p>
 */
@Component
@ViewScoped
@Getter
@Setter
public class ObraFormBean implements Serializable {

    private static final int ESCRITOR = 2;

    @Autowired
    private GestionObraServicio gestionObraServicio;
    @Autowired
    private GeneroServicio generoServicio;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private ArchivoServicio archivoServicio;
    @Autowired
    private LoginBean loginBean;

    @Value("#{param['idObra']}")
    private String parametroIdObra;

    private Long idObra;
    private boolean puedeEditar;
    /** Motivo por el que no se muestra el formulario (sin sesión, obra ajena o inexistente). */
    private String motivoBloqueo;

    private DatosObra datos = new DatosObra();
    private List<Genero> generosDisponibles = new ArrayList<>();
    private List<Escritor> coautoresDisponibles = new ArrayList<>();

    private String portadaGuardada;
    private String fragmentoGuardado;
    private String portadaPendiente;
    private String fragmentoPendiente;

    @PostConstruct
    public void inicializar() {
        if (!isEscritorAutenticado()) {
            motivoBloqueo = "Inicia sesión como escritor para publicar obras.";
            return;
        }
        Long idAutor = idAutor();
        idObra = ParametrosUrl.aLong(parametroIdObra);
        if (parametroIdObra != null && !parametroIdObra.isBlank() && idObra == null) {
            motivoBloqueo = "La obra no existe";
            return;
        }
        if (idObra != null) {
            try {
                datos = gestionObraServicio.obtenerDatosObra(idAutor, idObra);
            } catch (Exception e) {
                motivoBloqueo = e.getMessage();
                return;
            }
            portadaGuardada = datos.getUrlPortada();
            fragmentoGuardado = datos.getUrlFragmento();
        }
        cargarOpciones(idAutor);
        puedeEditar = true;
    }

    public boolean isEdicion() {
        return idObra != null;
    }

    public boolean isEscritorAutenticado() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == ESCRITOR
                && loginBean.getUsuario() != null && loginBean.getUsuario().getId() != null;
    }

    public Date getHoy() {
        return new Date();
    }

    public String getPortadaVistaPrevia() {
        return portadaPendiente != null ? portadaPendiente : portadaGuardada;
    }

    public String getFragmentoActual() {
        return fragmentoPendiente != null ? fragmentoPendiente : fragmentoGuardado;
    }

    public boolean isPortadaSinGuardar() {
        return portadaPendiente != null;
    }

    public boolean isFragmentoSinGuardar() {
        return fragmentoPendiente != null;
    }

    public void subirPortada(FileUploadEvent event) {
        try {
            String ruta = archivoServicio.guardarImagen(event.getFile());
            descartar(portadaPendiente, portadaGuardada);
            portadaPendiente = ruta;
            Mensajes.info("Portada cargada. Se guardará junto con la obra.");
        } catch (ArchivoInvalidoException e) {
            Mensajes.error(e.getMessage());
        } catch (IOException e) {
            Mensajes.error("No se pudo guardar la portada. Inténtalo de nuevo.");
        }
    }

    public void quitarPortada() {
        descartar(portadaPendiente, portadaGuardada);
        portadaPendiente = "";
        Mensajes.info("Se usará la portada por defecto al guardar.");
    }

    public void subirFragmento(FileUploadEvent event) {
        try {
            String ruta = archivoServicio.guardarPdf(event.getFile());
            descartar(fragmentoPendiente, fragmentoGuardado);
            fragmentoPendiente = ruta;
            Mensajes.info("Fragmento cargado. Se guardará junto con la obra.");
        } catch (ArchivoInvalidoException e) {
            Mensajes.error(e.getMessage());
        } catch (IOException e) {
            Mensajes.error("No se pudo guardar el PDF. Inténtalo de nuevo.");
        }
    }

    public String guardar() {
        if (!puedeEditar || !isEscritorAutenticado()) {
            Mensajes.error("No puedes guardar esta obra.");
            return null;
        }
        String fragmento = getFragmentoActual();
        if (fragmento == null || fragmento.isBlank()) {
            Mensajes.errorEn("formObra:fragmento", "El fragmento en PDF es obligatorio");
            return null;
        }
        datos.setUrlFragmento(fragmento);
        String portada = getPortadaVistaPrevia();
        datos.setUrlPortada(portada == null ? "" : portada);

        try {
            if (isEdicion()) {
                gestionObraServicio.actualizarObra(idAutor(), idObra, datos);
                // Los archivos sustituidos ya no los usa nadie
                if (portadaPendiente != null && !portadaPendiente.equals(portadaGuardada)) {
                    archivoServicio.eliminar(portadaGuardada);
                }
                if (fragmentoPendiente != null && !fragmentoPendiente.equals(fragmentoGuardado)) {
                    archivoServicio.eliminar(fragmentoGuardado);
                }
            } else {
                gestionObraServicio.crearObra(idAutor(), datos);
            }
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
            return null;
        }
        // Los archivos pendientes ya forman parte de la obra: no hay que borrarlos al destruir la vista
        portadaPendiente = null;
        fragmentoPendiente = null;
        Mensajes.info(isEdicion() ? "Los cambios de la obra se guardaron con éxito" : "La obra se publicó con éxito");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "mis_obras?faces-redirect=true";
    }

    /** Borra los archivos subidos que no llegaron a guardarse si el escritor abandona el formulario. */
    @PreDestroy
    public void descartarCambiosSinGuardar() {
        descartar(portadaPendiente, portadaGuardada);
        descartar(fragmentoPendiente, fragmentoGuardado);
        portadaPendiente = null;
        fragmentoPendiente = null;
    }

    private void cargarOpciones(Long idAutor) {
        generosDisponibles = new ArrayList<>(generoServicio.listarGeneros());
        generosDisponibles.sort(Comparator.comparing(Genero::getNombre, String.CASE_INSENSITIVE_ORDER));
        coautoresDisponibles = escritorServicio.listarEscritores().stream()
                .filter(e -> !e.getId().equals(idAutor))
                // Se mantienen los coautores actuales aunque estén inactivos, para que se vean en la edición
                .filter(e -> e.getEstado() == Estado.ACTIVO || datos.getCoautores().contains(e.getId()))
                .sorted(Comparator.comparing(Escritor::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Long idAutor() {
        return loginBean.getUsuario().getId();
    }

    /** Borra el archivo pendiente si es distinto del guardado (nunca el que usa la obra). */
    private void descartar(String pendiente, String guardado) {
        if (pendiente != null && !pendiente.isEmpty() && !pendiente.equals(guardado) && archivoServicio != null) {
            archivoServicio.eliminar(pendiente);
        }
    }
}
