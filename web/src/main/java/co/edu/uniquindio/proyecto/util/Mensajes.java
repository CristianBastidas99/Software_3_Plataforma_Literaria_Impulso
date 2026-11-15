package co.edu.uniquindio.proyecto.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Utilidad para mostrar mensajes de JSF ({@code p:messages} / {@code p:growl}) desde los beans.
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(String mensaje) {
        mostrar(mensaje, FacesMessage.SEVERITY_INFO);
    }

    public static void advertencia(String mensaje) {
        mostrar(mensaje, FacesMessage.SEVERITY_WARN);
    }

    public static void error(String mensaje) {
        mostrar(mensaje, FacesMessage.SEVERITY_ERROR);
    }

    public static void mostrar(String mensaje, FacesMessage.Severity severidad) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        if (contexto == null) {
            return;
        }
        contexto.addMessage(null, new FacesMessage(severidad, titulo(severidad), mensaje));
    }

    private static String titulo(FacesMessage.Severity severidad) {
        if (FacesMessage.SEVERITY_ERROR.equals(severidad) || FacesMessage.SEVERITY_FATAL.equals(severidad)) {
            return "Error";
        }
        if (FacesMessage.SEVERITY_WARN.equals(severidad)) {
            return "Aviso";
        }
        return "Información";
    }
}
