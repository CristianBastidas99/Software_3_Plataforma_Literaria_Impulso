package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.LoginServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@Component
@Scope("session")
@Getter
@Setter
public class LoginBean implements Serializable {

    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @Autowired
    private AdministradorServicio administradorServicio;
    @Autowired
    private LoginServicio loginServicio;

    private String email;
    private String password;
    private Boolean autenticado;
    private int tipo;
    private Usuario usuario;

    @PostConstruct
    public void inicializar() {
        usuario = new Usuario();
        autenticado = false;
        tipo = 0;
    }

    public String loginn() {
        try {
            if (camposVacios()) {
                return "login.xhtml";
            }
            if (!loginServicio.isEmailRegistrado(email)) {
                Mensajes.error("El correo electrónico no se encuentra registrado");
                return "login.xhtml";
            }
            Administrador administrador = administradorServicio.validaLogin(email, password);
            if (administrador != null) {
                return iniciarSesion(administrador, 1);
            }
            Escritor escritor = escritorServicio.validaLogin(email, password);
            if (escritor != null) {
                return iniciarSesion(escritor, 2);
            }
            Lector lector = lectorServicio.validaLogin(email, password);
            if (lector != null) {
                return iniciarSesion(lector, 3);
            }
            Mensajes.error("La contraseña es incorrecta");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        return "login.xhtml";
    }

    private String iniciarSesion(Usuario usuarioValido, int tipoUsuario) {
        usuario = usuarioValido;
        autenticado = true;
        tipo = tipoUsuario;
        return "index?faces-redirect=true";
    }

    private boolean camposVacios() {
        if (email == null || email.isEmpty()) {
            Mensajes.error("El correo electrónico no puede estar vacío");
            return true;
        }
        if (password == null || password.isEmpty()) {
            Mensajes.error("La contraseña no puede estar vacía");
            return true;
        }
        return false;
    }

    public String logout() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + "/index.xhtml");
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        return null;
    }
}
