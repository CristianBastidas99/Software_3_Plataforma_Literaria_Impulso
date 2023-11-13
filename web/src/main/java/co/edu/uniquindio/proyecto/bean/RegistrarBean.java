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
public class RegistrarBean implements Serializable{

    @Autowired
    private  EscritorServicio escritorServicio;
    @Autowired
    private  LectorServicio lectorServicio;
    @Autowired
    private  LoginServicio loginServicio;

    private String nombre;
    private String email;
    private String password;
    private String confirmPassword;
    private String rol;
    private Escritor escritor;
    private Lector lector;

    @PostConstruct
    public void inicializar(){
        escritor = new Escritor();
        lector = new Lector();
    }

    public void registrar() {
        try {
            if(!camposIsEmpy()) {
                if (!loginServicio.isEmailRegistrado(email)) {
                    if(password.equals(confirmPassword)){
                        if(rol.equals("escritor")){
                            escritor.setNombre(nombre);
                            escritor.setEmail(email);
                            escritor.setPassword(password);
                            escritor.setUrlImagen("autor.jpg");
                            escritor.setEstado(Estado.ACTIVO);
                            escritor.setBiografia("");

                            escritorServicio.registrarEscritor(escritor);
                            mostrarMensaje("Registro como escritor exitoso", FacesMessage.SEVERITY_INFO);
                            escritor = new Escritor();
                            nombre = "";
                            email = "";
                            password = "";
                            confirmPassword = "";

                        }else if(rol.equals("lector")){
                            lector.setNombre(nombre);
                            lector.setEmail(email);
                            lector.setPassword(password);
                            lector.setUrlImagen("autor.jpg");
                            lector.setEstado(Estado.ACTIVO);

                            lectorServicio.registrarLector(lector);
                            mostrarMensaje("Registro como lector exitoso", FacesMessage.SEVERITY_INFO);
                            lector = new Lector();
                            nombre = "";
                            email = "";
                            password = "";
                            confirmPassword = "";
                        }
                    }else{
                        throw new Exception("Los campos de contraseña no coiciden");
                    }
                }else{
                    throw new Exception("El email ya se encuentra en uso");
                }
            }
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private boolean camposIsEmpy(){
        if (nombre == null || nombre.isEmpty()) {
            mostrarMensaje("El nombre no puede estar vacío", FacesMessage.SEVERITY_ERROR);
            return true; // Detener el proceso de registro
        }else if (email == null || email.isEmpty()) {
            mostrarMensaje("El correo electrónico no puede estar vacío", FacesMessage.SEVERITY_ERROR);
            return true;
        }else if (password == null || password.isEmpty()) {
            mostrarMensaje("La contraseña no puede estar vacía", FacesMessage.SEVERITY_ERROR);
            return true;
        }else if (confirmPassword == null || confirmPassword.isEmpty()) {
            mostrarMensaje("La confirmación de la contraseña no puede estar vacía", FacesMessage.SEVERITY_ERROR);
            return true;
        }
        return false;
    }

    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

}
