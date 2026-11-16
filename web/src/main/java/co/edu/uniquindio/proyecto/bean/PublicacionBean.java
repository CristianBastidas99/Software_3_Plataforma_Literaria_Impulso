package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.CorreoServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class PublicacionBean implements Serializable {

    private static final int ESCRITOR = 2;
    private static final int LECTOR = 3;

    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private InteraccionServicio interaccionServicio;
    @Autowired
    private CorreoServicio correoServicio;
    @Autowired
    private LoginBean loginBean;

    @Value("#{param['idProd']}")
    private String idProducto;

    private Publicacion publicacion;
    private List<Escritor> autores = new ArrayList<>();
    private List<Genero> generos = new ArrayList<>();
    /** El usuario con sesión es autor o coautor de la obra. */
    private boolean autor;

    private List<Comentario> comentarios = new ArrayList<>();
    private String nuevoComentario;
    private long vistas;
    private long recomendaciones;
    private boolean recomendada;

    /**
     * Indica si la publicación existe y el usuario puede verla; si es falso la vista muestra un mensaje
     * en lugar del detalle. Una obra retirada solo la ven sus autores.
     */
    private boolean encontrada;

    @PostConstruct
    public void inicializar() {
        encontrada = false;
        Long id = ParametrosUrl.aLong(idProducto);
        if (id == null) {
            return;
        }
        try {
            publicacion = publicacionServicio.obtenerPublicacion(id);
        } catch (Exception e) {
            publicacion = null;
            return;
        }
        if (publicacion.getObraLiteraria() != null) {
            Long idObra = publicacion.getObraLiteraria().getId();
            autores = publicacionServicio.obtenerEscritoresDeObra(idObra);
            generos = new ArrayList<>(publicacion.getObraLiteraria().getGeneros());
        }
        autor = esAutorConSesion();
        encontrada = isPublicada() || autor;
        if (!encontrada) {
            publicacion = null;
            return;
        }
        if (isPublicada() && isLector()) {
            interaccionServicio.registrarVista(idUsuario(), publicacion.getId());
        }
        cargarInteraccion();
    }

    /** Lectores y escritores con sesión pueden comentar una publicación publicada. */
    public boolean isPuedeComentar() {
        return isPublicada() && Boolean.TRUE.equals(loginBean.getAutenticado())
                && (loginBean.getTipo() == LECTOR || loginBean.getTipo() == ESCRITOR);
    }

    public boolean isLector() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && loginBean.getTipo() == LECTOR && idUsuario() != null;
    }

    public int getComentarioMax() {
        return InteraccionServicio.COMENTARIO_MAX;
    }

    public void comentar() {
        if (!isPuedeComentar()) {
            Mensajes.error("Inicia sesión como lector o escritor para comentar");
            return;
        }
        try {
            Comentario guardado = interaccionServicio.comentar(publicacion.getId(), loginBean.getUsuario(), nuevoComentario);
            if (!guardado.getContenido().equals(nuevoComentario.trim())) {
                Mensajes.advertencia("Tu comentario se publicó, pero algunas palabras se censuraron.");
            } else {
                Mensajes.info("Tu comentario se publicó.");
            }
            nuevoComentario = "";
            avisarAutores(guardado);
            cargarInteraccion();
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
    }

    /**
     * Avisa por correo (en segundo plano) a los autores activos de la obra, salvo al que comenta.
     * Se envía el texto ya censurado.
     */
    private void avisarAutores(Comentario comentario) {
        Long idQuienComenta = loginBean.getTipo() == ESCRITOR ? loginBean.getUsuario().getId() : null;
        String nombreQuienComenta = loginBean.getUsuario().getNombre();
        for (Escritor autor : autores) {
            if (autor.getEstado() == Estado.ACTIVO && !autor.getId().equals(idQuienComenta)) {
                correoServicio.enviarAvisoComentario(autor.getNombre(), autor.getEmail(), nombreQuienComenta,
                        publicacion.getTitulo(), comentario.getContenido(), publicacion.getId());
            }
        }
    }

    public void eliminarComentario(Long idComentario) {
        try {
            interaccionServicio.eliminarComentario(idComentario, loginBean.getUsuario());
            Mensajes.info("El comentario se eliminó.");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        cargarInteraccion();
    }

    public void alternarRecomendacion() {
        if (!isLector()) {
            Mensajes.error("Inicia sesión como lector para recomendar publicaciones");
            return;
        }
        try {
            boolean activa = interaccionServicio.alternarRecomendacion(idUsuario(), publicacion.getId());
            Mensajes.info(activa ? "Recomendaste esta publicación." : "Ya no recomiendas esta publicación.");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        cargarInteraccion();
    }

    public boolean esMio(Comentario comentario) {
        return interaccionServicio.esAutorDeComentario(comentario, loginBean.getUsuario());
    }

    /** Nombre de quien escribió el comentario (lector o escritor). */
    public String nombreAutor(Comentario comentario) {
        if (comentario.getLector() != null) {
            return comentario.getLector().getNombre();
        }
        return comentario.getEscritor() != null ? comentario.getEscritor().getNombre() : "Anónimo";
    }

    public String imagenAutor(Comentario comentario) {
        if (comentario.getLector() != null) {
            return comentario.getLector().getUrlImagen();
        }
        return comentario.getEscritor() != null ? comentario.getEscritor().getUrlImagen() : null;
    }

    public boolean esDeEscritor(Comentario comentario) {
        return comentario.getLector() == null && comentario.getEscritor() != null;
    }

    private void cargarInteraccion() {
        Long id = publicacion.getId();
        comentarios = interaccionServicio.listarComentarios(id);
        vistas = interaccionServicio.contarVistas(id);
        recomendaciones = interaccionServicio.contarRecomendaciones(id);
        recomendada = isLector() && interaccionServicio.haRecomendado(idUsuario(), id);
    }

    private Long idUsuario() {
        return loginBean.getUsuario() == null ? null : loginBean.getUsuario().getId();
    }

    public boolean isPublicada() {
        return publicacion != null && publicacion.getEstado() == Estado.PUBLICADO;
    }

    private boolean esAutorConSesion() {
        if (!Boolean.TRUE.equals(loginBean.getAutenticado()) || loginBean.getTipo() != ESCRITOR
                || loginBean.getUsuario() == null || loginBean.getUsuario().getId() == null) {
            return false;
        }
        Long idUsuario = loginBean.getUsuario().getId();
        return autores.stream().anyMatch(e -> idUsuario.equals(e.getId()));
    }
}
