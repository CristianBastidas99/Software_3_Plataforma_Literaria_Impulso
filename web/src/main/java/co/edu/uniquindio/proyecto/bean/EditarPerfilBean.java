package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.archivos.ArchivoInvalidoException;
import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@Component
@ViewScoped
@Getter
@Setter
public class EditarPerfilBean implements Serializable {

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @Value(value = "#{loginBean.usuario}")
    private Usuario usuarioSesion;
    @Value(value = "#{loginBean.tipo}")
    private int tipo;
    @Autowired
    private ArchivoServicio archivoServicio;
    private String titulo;
    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;
    private String imagenPerfil;

    @PostConstruct
    public void inicializar() {
        if (tipo == 2) {
            titulo = "Escritor";
        } else if (tipo == 3) {
            titulo = "Lector";
        }
        contrasenaActual = "";
        nuevaContrasena = "";
        confirmarContrasena = "";
        imagenPerfil = "";
    }

    public void guardarCambios() {
        try {
            if (tipo == 2) {
                escritorServicio.actualizarEscritor((Escritor) usuarioSesion);
                Mensajes.info("Los cambios se guardaron con éxito");
            } else if (tipo == 3) {
                lectorServicio.actualizarLector((Lector) usuarioSesion);
                Mensajes.info("Los cambios se guardaron con éxito");
            }
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
    }

    public void cambiarContrasena() {
        try {
            if (!contrasenaActual.equals(usuarioSesion.getPassword())) {
                Mensajes.error("La contraseña actual es incorrecta");
                return;
            }
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                Mensajes.error("La nueva contraseña no coincide con la confirmación");
                return;
            }
            if (tipo == 2) {
                Escritor escritor = (Escritor) usuarioSesion;
                escritor.setPassword(nuevaContrasena);
                escritorServicio.actualizarEscritor(escritor);
                Mensajes.info("La contraseña se cambió con éxito");
            } else if (tipo == 3) {
                Lector lector = (Lector) usuarioSesion;
                lector.setPassword(nuevaContrasena);
                lectorServicio.actualizarLector(lector);
                Mensajes.info("La contraseña se cambió con éxito");
            }
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
    }

    public void subirImagenes(FileUploadEvent event) {
        UploadedFile imagen = event.getFile();
        try {
            imagenPerfil = archivoServicio.guardarImagen(imagen);
            usuarioSesion.setUrlImagen(imagenPerfil);
            Mensajes.info("La imagen se cargó correctamente. Pulsa \"Guardar Cambios\" para conservarla.");
        } catch (ArchivoInvalidoException e) {
            Mensajes.error(e.getMessage());
        } catch (IOException e) {
            Mensajes.error("No se pudo guardar la imagen. Inténtalo de nuevo.");
        }
    }
}
