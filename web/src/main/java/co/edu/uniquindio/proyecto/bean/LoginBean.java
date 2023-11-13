package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
@ViewScoped
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
    private Escritor escritor;
    private Lector lector;
    private Administrador administrador;

    @PostConstruct
    public void login(){
        escritor = new Escritor();
        lector = new Lector();
        administrador = new Administrador();
    }

    public void loginn() {
        try {
            if(!camposIsEmpy()) {
                if (loginServicio.isEmailRegistrado(email)) {
                    administrador = administradorServicio.validaLogin(email, password);
                    if(administrador!= null){
                        mostrarMensaje("Login Exitoso Administrador", FacesMessage.SEVERITY_INFO);
                    }
                    escritor = escritorServicio.validaLogin(email, password);
                    if(escritor!= null){
                        mostrarMensaje("Login Exitoso Escritor", FacesMessage.SEVERITY_INFO);
                    }
                    lector = lectorServicio.validaLogin(email, password);
                    if(lector!= null){
                        mostrarMensaje("Login Exitoso Lector", FacesMessage.SEVERITY_INFO);
                    }
                    if (administrador == null && escritor == null && lector == null) {
                        mostrarMensaje("La contraseña es incorrecta", FacesMessage.SEVERITY_ERROR);
                    }else{
                        email = "";
                        password = "";
                        escritor = new Escritor();
                        lector = new Lector();
                        administrador = new Administrador();
                    }
                }else{
                    throw new Exception("El email no se encuentra registrado");
                }
            }
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
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

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

}
