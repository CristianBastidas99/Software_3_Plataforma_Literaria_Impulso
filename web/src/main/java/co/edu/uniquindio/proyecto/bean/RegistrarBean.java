package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.LoginServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import co.edu.uniquindio.proyecto.util.ValidacionUsuario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@Component
@ViewScoped
@Getter
@Setter
public class RegistrarBean implements Serializable {

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @Autowired
    private LoginServicio loginServicio;

    private String nombre;
    private String email;
    private String password;
    private String confirmPassword;
    private String rol;

    public void registrar() {
        try {
            if (!camposValidos()) {
                return;
            }
            String correo = ValidacionUsuario.normalizarEmail(email);
            String nombreLimpio = nombre.trim();
            if (loginServicio.isEmailRegistrado(correo)) {
                Mensajes.error("El correo electrónico ya se encuentra en uso");
                return;
            }
            if (!password.equals(confirmPassword)) {
                Mensajes.error("Las contraseñas no coinciden");
                return;
            }
            if ("escritor".equals(rol)) {
                Escritor escritor = new Escritor();
                escritor.setNombre(nombreLimpio);
                escritor.setEmail(correo);
                escritor.setPassword(password);
                escritor.setUrlImagen("");
                escritor.setEstado(Estado.ACTIVO);
                escritor.setBiografia("");
                escritorServicio.registrarEscritor(escritor);
                Mensajes.info("Registro como escritor exitoso");
                limpiarCampos();
            } else if ("lector".equals(rol)) {
                Lector lector = new Lector();
                lector.setNombre(nombreLimpio);
                lector.setEmail(correo);
                lector.setPassword(password);
                lector.setUrlImagen("");
                lector.setEstado(Estado.ACTIVO);
                lectorServicio.registrarLector(lector);
                Mensajes.info("Registro como lector exitoso");
                limpiarCampos();
            }
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
    }

    private void limpiarCampos() {
        nombre = "";
        email = "";
        password = "";
        confirmPassword = "";
    }

    private boolean camposValidos() {
        String error = ValidacionUsuario.validarNombre(nombre);
        if (error == null) {
            error = ValidacionUsuario.validarEmail(email);
        }
        if (error == null) {
            error = ValidacionUsuario.validarPassword(password);
        }
        if (error == null && (confirmPassword == null || confirmPassword.isEmpty())) {
            error = "La confirmación de la contraseña no puede estar vacía";
        }
        if (error != null) {
            Mensajes.error(error);
            return false;
        }
        return true;
    }
}
