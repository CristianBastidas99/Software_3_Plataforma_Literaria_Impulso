package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.servicios.RecuperacionServicio;
import co.edu.uniquindio.proyecto.util.Mensajes;
import co.edu.uniquindio.proyecto.util.ValidacionUsuario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

/** Página "¿Olvidaste tu contraseña?" ({@code recuperar.xhtml}): pide el correo y envía el enlace. */
@Component
@ViewScoped
@Getter
@Setter
public class RecuperarBean implements Serializable {

    @Autowired
    private RecuperacionServicio recuperacionServicio;

    private String email;
    private boolean enviado;

    public void enviar() {
        String error = ValidacionUsuario.validarEmail(email);
        if (error != null) {
            Mensajes.error(error);
            return;
        }
        String correo = ValidacionUsuario.normalizarEmail(email);
        recuperacionServicio.solicitarRecuperacion(correo);
        // El mismo mensaje exista o no la cuenta, para no revelar qué correos están registrados
        email = correo;
        enviado = true;
    }

    public int getMinutosValidez() {
        return recuperacionServicio.getMinutosValidez();
    }
}
