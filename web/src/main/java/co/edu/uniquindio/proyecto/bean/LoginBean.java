package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@Component
@Scope("session")
@Getter
@Setter
public class LoginBean implements Serializable{

    @Autowired
    private  EscritorServicio escritorServicio;
    @Autowired
    private  LectorServicio lectorServicio;
    @Autowired
    private  AdministradorServicio administradorServicio;
    @Autowired
    private  LoginServicio loginServicio;

    private String email;
    private String password;
    private Boolean autenticado;
    private int tipo;
    private Usuario usuario;

    @PostConstruct
    public void inicializar(){
        usuario = new Usuario();
        autenticado = false;
        tipo = 0;
    }

    public String loginn() {
        try {
            if(!camposIsEmpy()) {
                if (loginServicio.isEmailRegistrado(email)) {
                    Administrador administrador = administradorServicio.validaLogin(email, password);
                    if(administrador!= null){
                        mostrarMensaje("Login Exitoso Administrador", FacesMessage.SEVERITY_INFO);
                        usuario = administrador;
                        autenticado = true;
                        tipo = 1;
                        return "index.xhtml";
                    }
                    Escritor escritor = escritorServicio.validaLogin(email, password);
                    if(escritor!= null){
                        mostrarMensaje("Login Exitoso Escritor", FacesMessage.SEVERITY_INFO);
                        usuario = escritor;
                        autenticado = true;
                        tipo = 2;
                        return "index.xhtml";
                    }
                    Lector lector = lectorServicio.validaLogin(email, password);
                    if(lector!= null){
                        mostrarMensaje("Login Exitoso Lector", FacesMessage.SEVERITY_INFO);
                        usuario = lector;
                        autenticado = true;
                        tipo = 3;
                        return "index.xhtml";
                    }
                    if (administrador == null && escritor == null && lector == null) {
                        mostrarMensaje("La contraseña es incorrecta", FacesMessage.SEVERITY_ERROR);
                    }
                }else{
                    throw new Exception("El email no se encuentra registrado");
                }
            }
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
        return "login.xhtml";
    }

    private boolean camposIsEmpy(){
        if (email == null || email.isEmpty()) {
            mostrarMensaje("El correo electrónico no puede estar vacío", FacesMessage.SEVERITY_ERROR);
            return true;
        }else if (password == null || password.isEmpty()) {
            mostrarMensaje("La contraseña no puede estar vacía", FacesMessage.SEVERITY_ERROR);
            return true;
        }
        return false;
    }

    public String logout(){
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        // Redirigir a la página de inicio
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            externalContext.redirect(request.getContextPath() + "/index.xhtml");
        } catch (Exception e) {
            // Manejar excepciones si es necesario
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }

        return null;
    }

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

}
