package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.archivos.ArchivoInvalidoException;
import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.LoginServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import co.edu.uniquindio.proyecto.util.ValidacionUsuario;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.view.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

/**
 * Edición del perfil del escritor o lector autenticado: nombre, correo, biografía (escritor),
 * imagen y contraseña.
 *
 * <p>El formulario trabaja sobre copias de los datos. Al guardar se carga el usuario desde la base
 * de datos, se aplican los cambios validados y se actualiza el usuario de la sesión, de modo que un
 * intento inválido no deja la sesión modificada y el menú refleja los cambios al instante.</p>
 */
@Component
@ViewScoped
@Getter
@Setter
public class EditarPerfilBean implements Serializable {

    private static final int ESCRITOR = 2;
    private static final int LECTOR = 3;

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @Autowired
    private LoginServicio loginServicio;
    @Autowired
    private ArchivoServicio archivoServicio;
    @Autowired
    private LoginBean loginBean;

    private int tipo;
    private Long idUsuario;
    private String titulo;

    // Copias editables de los datos del perfil
    private String nombre;
    private String email;
    private String biografia;
    /** Imagen guardada en la base de datos al abrir la página o tras el último guardado. */
    private String imagenGuardada;
    /** Imagen elegida en el formulario (subida o quitada) pendiente de guardar; {@code null} si no hay cambios. */
    private String imagenPendiente;

    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;

    @PostConstruct
    public void inicializar() {
        tipo = loginBean.getTipo();
        Usuario usuario = loginBean.getUsuario();
        if (!isPuedeEditar() || usuario == null || usuario.getId() == null) {
            return;
        }
        idUsuario = usuario.getId();
        titulo = tipo == ESCRITOR ? "Escritor" : "Lector";
        cargarDesde(usuario);
        limpiarContrasenas();
    }

    /** Solo escritores y lectores autenticados pueden editar su perfil. */
    public boolean isPuedeEditar() {
        return Boolean.TRUE.equals(loginBean.getAutenticado()) && (tipo == ESCRITOR || tipo == LECTOR);
    }

    public boolean isEscritor() {
        return tipo == ESCRITOR;
    }

    /** Imagen a mostrar en la vista previa: la pendiente si hay una, si no la guardada. */
    public String getImagenVistaPrevia() {
        return imagenPendiente != null ? imagenPendiente : imagenGuardada;
    }

    public boolean isHayImagenPendiente() {
        return imagenPendiente != null;
    }

    public void subirImagen(FileUploadEvent event) {
        try {
            String ruta = archivoServicio.guardarImagen(event.getFile());
            descartarImagenPendiente();
            imagenPendiente = ruta;
            Mensajes.info("Imagen cargada. Pulsa \"Guardar cambios\" para conservarla.");
        } catch (ArchivoInvalidoException e) {
            Mensajes.error(e.getMessage());
        } catch (IOException e) {
            Mensajes.error("No se pudo guardar la imagen. Inténtalo de nuevo.");
        }
    }

    /** Vuelve a la imagen por defecto (se aplica al guardar). */
    public void quitarImagen() {
        descartarImagenPendiente();
        imagenPendiente = "";
        Mensajes.info("Se usará la imagen por defecto. Pulsa \"Guardar cambios\" para confirmarlo.");
    }

    public void guardarCambios() {
        if (!isPuedeEditar()) {
            Mensajes.error("Debes iniciar sesión como escritor o lector para editar tu perfil");
            return;
        }
        if (!datosValidos()) {
            return;
        }
        try {
            Usuario actual = obtenerUsuario();
            String emailNuevo = ValidacionUsuario.normalizarEmail(email);
            if (!emailNuevo.equalsIgnoreCase(actual.getEmail()) && loginServicio.isEmailRegistrado(emailNuevo)) {
                Mensajes.error("El correo electrónico ya está en uso por otro usuario");
                return;
            }

            String imagenAnterior = actual.getUrlImagen();
            actual.setNombre(nombre.trim());
            actual.setEmail(emailNuevo);
            if (imagenPendiente != null) {
                actual.setUrlImagen(imagenPendiente);
            }
            if (actual instanceof Escritor) {
                ((Escritor) actual).setBiografia(biografia == null ? "" : biografia.trim());
            }

            Usuario guardado = guardar(actual);
            loginBean.setUsuario(guardado);

            if (imagenPendiente != null && !imagenPendiente.equals(imagenAnterior)) {
                archivoServicio.eliminar(imagenAnterior);
            }
            imagenPendiente = null;
            cargarDesde(guardado);
            Mensajes.info("Los cambios se guardaron con éxito");
        } catch (Exception e) {
            Mensajes.error("No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    public void cambiarContrasena() {
        if (!isPuedeEditar()) {
            Mensajes.error("Debes iniciar sesión como escritor o lector para cambiar tu contraseña");
            return;
        }
        try {
            Usuario actual = obtenerUsuario();
            if (!Contrasenas.coincide(contrasenaActual, actual.getPassword())) {
                Mensajes.error("La contraseña actual es incorrecta");
                return;
            }
            String error = ValidacionUsuario.validarPassword(nuevaContrasena);
            if (error != null) {
                Mensajes.error(error);
                return;
            }
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                Mensajes.error("La nueva contraseña no coincide con la confirmación");
                return;
            }
            if (nuevaContrasena.equals(contrasenaActual)) {
                Mensajes.error("La nueva contraseña debe ser distinta de la actual");
                return;
            }
            actual.setPassword(nuevaContrasena);
            loginBean.setUsuario(guardar(actual));
            Mensajes.info("La contraseña se cambió con éxito");
        } catch (Exception e) {
            Mensajes.error("No se pudo cambiar la contraseña: " + e.getMessage());
        } finally {
            limpiarContrasenas();
        }
    }

    /** Si el usuario abandona la página sin guardar, se borra la imagen que subió y no llegó a usarse. */
    @PreDestroy
    public void descartarCambiosSinGuardar() {
        descartarImagenPendiente();
    }

    private boolean datosValidos() {
        String error = ValidacionUsuario.validarNombre(nombre);
        if (error == null) {
            error = ValidacionUsuario.validarEmail(email);
        }
        if (error == null && isEscritor()) {
            error = ValidacionUsuario.validarBiografia(biografia);
        }
        if (error != null) {
            Mensajes.error(error);
            return false;
        }
        return true;
    }

    private Usuario obtenerUsuario() throws Exception {
        return tipo == ESCRITOR ? escritorServicio.obtenerEscritor(idUsuario) : lectorServicio.obtenerLector(idUsuario);
    }

    private Usuario guardar(Usuario usuario) throws Exception {
        return tipo == ESCRITOR
                ? escritorServicio.actualizarEscritor((Escritor) usuario)
                : lectorServicio.actualizarLector((Lector) usuario);
    }

    private void cargarDesde(Usuario usuario) {
        nombre = usuario.getNombre();
        email = usuario.getEmail();
        imagenGuardada = usuario.getUrlImagen();
        biografia = usuario instanceof Escritor ? ((Escritor) usuario).getBiografia() : null;
    }

    /** Borra del disco la imagen subida en esta página que todavía no se ha guardado. */
    private void descartarImagenPendiente() {
        if (imagenPendiente != null && !imagenPendiente.equals(imagenGuardada) && archivoServicio != null) {
            archivoServicio.eliminar(imagenPendiente);
        }
        imagenPendiente = null;
    }

    private void limpiarContrasenas() {
        contrasenaActual = "";
        nuevaContrasena = "";
        confirmarContrasena = "";
    }
}
