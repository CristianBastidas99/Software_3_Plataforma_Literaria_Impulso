package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.ObraLiteraria;
import co.edu.uniquindio.proyecto.servicios.GestionObraServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Página "Mis obras": las obras del escritor autenticado (como autor o coautor) con su estado,
 * y las acciones de editar, retirar y volver a publicar.
 */
@Component
@ViewScoped
@Getter
public class MisObrasBean implements Serializable {

    private static final int ESCRITOR = 2;

    @Autowired
    private GestionObraServicio gestionObraServicio;
    @Autowired
    private LoginBean loginBean;

    private List<ObraLiteraria> obras = new ArrayList<>();

    @PostConstruct
    public void inicializar() {
        cargar();
    }

    public boolean isEscritorAutenticado() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == ESCRITOR
                && loginBean.getUsuario() != null && loginBean.getUsuario().getId() != null;
    }

    public void retirar(Long idObra) {
        try {
            gestionObraServicio.retirarObra(idAutor(), idObra);
            Mensajes.info("La obra se retiró. Ya no aparece en inicio, en las búsquedas ni en tu perfil público.");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        cargar();
    }

    public void volverAPublicar(Long idObra) {
        try {
            gestionObraServicio.volverAPublicarObra(idAutor(), idObra);
            Mensajes.info("La obra vuelve a estar publicada.");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        cargar();
    }

    public boolean isPublicada(ObraLiteraria obra) {
        return obra.getEstado() == Estado.PUBLICADO;
    }

    public boolean isRetirada(ObraLiteraria obra) {
        return obra.getEstado() == Estado.INACTIVO;
    }

    /** Texto y estilo de la etiqueta de estado. */
    public String textoEstado(ObraLiteraria obra) {
        if (obra.getEstado() == null) {
            return "Sin estado";
        }
        switch (obra.getEstado()) {
            case PUBLICADO:
                return "Publicada";
            case INACTIVO:
                return "Retirada";
            case PENDIENTE:
                return "Pendiente de revisión";
            case RECHAZADO:
                return "Ocultada por el administrador";
            case APROBADO:
                return "Aprobada";
            default:
                return obra.getEstado().name();
        }
    }

    public String severidadEstado(ObraLiteraria obra) {
        if (obra.getEstado() == Estado.PUBLICADO) {
            return "success";
        }
        if (obra.getEstado() == Estado.INACTIVO || obra.getEstado() == Estado.RECHAZADO) {
            return "danger";
        }
        return "warning";
    }

    private void cargar() {
        obras = isEscritorAutenticado() ? gestionObraServicio.listarObrasDeAutor(idAutor()) : new ArrayList<>();
    }

    private Long idAutor() {
        return isEscritorAutenticado() ? loginBean.getUsuario().getId() : null;
    }
}
