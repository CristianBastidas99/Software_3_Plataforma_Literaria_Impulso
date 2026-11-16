package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.servicios.RecuperacionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

/**
 * Página del enlace de recuperación ({@code restablecer.xhtml?token=...}): comprueba el enlace al
 * abrirla y permite elegir la contraseña nueva.
 */
@Component
@ViewScoped
@Getter
@Setter
public class RestablecerBean implements Serializable {

    @Autowired
    private RecuperacionServicio recuperacionServicio;

    /** Solo está en la URL al abrir la página; se conserva en el bean para el envío del formulario. */
    @Value("#{param['token']}")
    private String token;

    private boolean tokenValido;
    private String email;
    private String motivoInvalido;
    private String password;
    private String confirmPassword;

    @PostConstruct
    public void inicializar() {
        try {
            email = recuperacionServicio.validarToken(token);
            tokenValido = true;
        } catch (Exception e) {
            tokenValido = false;
            motivoInvalido = e.getMessage();
        }
    }

    public String cambiar() {
        try {
            recuperacionServicio.restablecerContrasena(token, password, confirmPassword);
            Mensajes.info("Tu contraseña se cambió. Ya puedes iniciar sesión con la nueva.");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "login?faces-redirect=true";
        } catch (Exception e) {
            Mensajes.error(e.getMessage());
            // Si el enlace dejó de valer mientras la página estaba abierta, se oculta el formulario
            try {
                recuperacionServicio.validarToken(token);
            } catch (Exception invalido) {
                tokenValido = false;
                motivoInvalido = invalido.getMessage();
            }
            return null;
        }
    }
}
