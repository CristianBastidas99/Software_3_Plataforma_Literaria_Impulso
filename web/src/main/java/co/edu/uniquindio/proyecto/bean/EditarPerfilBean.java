package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.servicios.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.ResponsiveOption;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@ViewScoped
@Getter
@Setter
public class EditarPerfilBean implements Serializable{

    @Autowired
    private  EscritorServicio escritorServicio;
    @Autowired
    private  LectorServicio lectorServicio;
    @Value(value = "#{loginBean.usuario}")
    private Usuario usuarioSesion;
    @Value(value = "#{loginBean.tipo}")
    private int tipo;
    @Value("${upload.url}")
    private String urlImagenes;
    private String titulo;
    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;
    private String imagenPerfil;

    @PostConstruct
    public void inicializar(){
        if(tipo == 2){
            titulo = "Escritor";
        }else if(tipo == 3){
            titulo = "Lector";
        }
        contrasenaActual = "";
        nuevaContrasena = "";
        confirmarContrasena = "";
        imagenPerfil = "";
    }

    public void guardarCambios() {
        try {
            if(tipo == 2){
                Escritor escritor = (Escritor) usuarioSesion;
                escritorServicio.actualizarEscritor(escritor);
                mostrarMensaje("Se actualizacon con exito", FacesMessage.SEVERITY_INFO);
            }else if(tipo == 3){
                Lector lector = (Lector) usuarioSesion;
                lectorServicio.actualizarLector(lector);
                mostrarMensaje("Se actualizacon con exito", FacesMessage.SEVERITY_INFO);
            }
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void cambiarContrasena(){
        try {
            if(contrasenaActual.equals(usuarioSesion.getPassword())){
                if(nuevaContrasena.equals(confirmarContrasena)){
                    if(tipo == 2){
                        Escritor escritor = (Escritor) usuarioSesion;
                        escritor.setPassword(nuevaContrasena);
                        escritorServicio.actualizarEscritor(escritor);
                        mostrarMensaje("Se actualizacon con exito", FacesMessage.SEVERITY_INFO);
                    }else if(tipo == 3){
                        Lector lector = (Lector) usuarioSesion;
                        lectorServicio.actualizarLector(lector);
                        lector.setPassword(nuevaContrasena);
                        mostrarMensaje("Se actualizacon con exito", FacesMessage.SEVERITY_INFO);
                    }
                }else{
                    mostrarMensaje("La nueva contraseña no coincide con la confirmacion", FacesMessage.SEVERITY_ERROR);
                }
            }else{
                mostrarMensaje("La contaseña actual es incorrecta", FacesMessage.SEVERITY_ERROR);
            }
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void subirImagenes(FileUploadEvent event){
        UploadedFile imagen = event.getFile();
        imagenPerfil = subirImagen(imagen);
        if(imagenPerfil!=null){
            usuarioSesion.setUrlImagen(imagenPerfil);
            mostrarMensaje("La imagen se cargo", FacesMessage.SEVERITY_INFO);
        }else{
            mostrarMensaje("No se pudo cargar la imagen", FacesMessage.SEVERITY_ERROR);
        }
    }

    public String subirImagen2(UploadedFile imagen){
        try {
            File archivo = new File(urlImagenes + "/" + imagen.getFileName());
            OutputStream outputStream = new FileOutputStream(archivo);
            IOUtils.copy(imagen.getInputStream(), outputStream);
            return imagen.getFileName();
        }catch (Exception e){
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
        return null;
    }

    private String subirImagen(UploadedFile imagen) {
        String nombreArchivo = null;
        InputStream input = null;
        OutputStream output = null;

        try {
            // Obtener el nombre del archivo original
            String nombreOriginal = imagen.getFileName();

            // Generar un nombre único para evitar conflictos
            nombreArchivo = generarNombreUnico(nombreOriginal);

            // Ruta donde se guardará el archivo en el servidor
            String rutaDestino = urlImagenes + nombreArchivo;

            // Crear un InputStream a partir del UploadedFile
            input = imagen.getInputStream();

            // Crear un OutputStream para escribir el archivo en el servidor
            output = new FileOutputStream(new File(rutaDestino));

            // Utilizar IOUtils para copiar el contenido del InputStream al OutputStream
            IOUtils.copy(input, output);

        } catch (IOException e) {
            // Manejar excepciones si es necesario
            mostrarMensaje(e.getMessage(), FacesMessage.SEVERITY_ERROR);
            nombreArchivo = null;
        } finally {
            // Cerrar InputStream y OutputStream
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

        return nombreArchivo;
    }

    private String generarNombreUnico(String nombreOriginal) {
        // Lógica para generar un nombre único (puedes implementar la tuya)
        // Por ejemplo, podrías agregar un timestamp al nombre original
        long timestamp = System.currentTimeMillis();
        return timestamp + "_" + nombreOriginal;
    }


    private void mostrarMensaje(String mensaje, FacesMessage.Severity severity) {
        FacesMessage facesMsg = new FacesMessage(severity, "Alerta", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
}
