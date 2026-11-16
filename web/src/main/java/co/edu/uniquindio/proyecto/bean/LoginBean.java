package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.seguridad.RegistroSesiones;
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
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Component
@Scope("session")
@Getter
@Setter
public class LoginBean implements Serializable {

    public static final int ADMINISTRADOR = 1;
    public static final int ESCRITOR = 2;
    public static final int LECTOR = 3;

    @Autowired
    private LoginServicio loginServicio;
    @Autowired
    private RegistroSesiones registroSesiones;

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
            // El mismo mensaje si el correo no existe o la contraseña no coincide
            Usuario valido = loginServicio.autenticar(email, password).orElse(null);
            if (valido instanceof Administrador) {
                return iniciarSesion(valido, ADMINISTRADOR);
            }
            if (valido instanceof Escritor) {
                return iniciarSesion(valido, ESCRITOR);
            }
            if (valido instanceof Lector) {
                return iniciarSesion(valido, LECTOR);
            }
            Mensajes.error(LoginServicio.CREDENCIALES_INCORRECTAS);
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
        }
        return "login.xhtml";
    }

    private String iniciarSesion(Usuario usuarioValido, int tipoUsuario) {
        // Nuevo identificador de sesión al autenticarse (evita la fijación de sesión); conserva los datos
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.changeSessionId();
        password = null;
        usuario = usuarioValido;
        autenticado = true;
        tipo = tipoUsuario;
        // Para poder expulsarlo si el administrador lo bloquea mientras está dentro
        registroSesiones.registrar(tipoUsuario, usuarioValido.getId(), request.getSession());
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
