package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.LoginServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
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
            if (camposVacios()) {
                return;
            }
            if (loginServicio.isEmailRegistrado(email)) {
                Mensajes.error("El correo electrónico ya se encuentra en uso");
                return;
            }
            if (!password.equals(confirmPassword)) {
                Mensajes.error("Las contraseñas no coinciden");
                return;
            }
            if ("escritor".equals(rol)) {
                Escritor escritor = new Escritor();
                escritor.setNombre(nombre);
                escritor.setEmail(email);
                escritor.setPassword(password);
                escritor.setUrlImagen("");
                escritor.setEstado(Estado.ACTIVO);
                escritor.setBiografia("");
                escritorServicio.registrarEscritor(escritor);
                Mensajes.info("Registro como escritor exitoso");
                limpiarCampos();
            } else if ("lector".equals(rol)) {
                Lector lector = new Lector();
                lector.setNombre(nombre);
                lector.setEmail(email);
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

    private boolean camposVacios() {
        if (nombre == null || nombre.isEmpty()) {
            Mensajes.error("El nombre no puede estar vacío");
            return true;
        }
        if (email == null || email.isEmpty()) {
            Mensajes.error("El correo electrónico no puede estar vacío");
            return true;
        }
        if (password == null || password.isEmpty()) {
            Mensajes.error("La contraseña no puede estar vacía");
            return true;
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            Mensajes.error("La confirmación de la contraseña no puede estar vacía");
            return true;
        }
        return false;
    }
}
