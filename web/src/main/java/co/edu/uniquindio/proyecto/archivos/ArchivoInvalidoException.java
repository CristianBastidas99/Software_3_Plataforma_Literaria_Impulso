package co.edu.uniquindio.proyecto.archivos;

/**
 * El archivo subido no cumple las reglas (tipo, contenido o tamaño). El mensaje se puede mostrar al usuario.
 */
public class ArchivoInvalidoException extends Exception {

    public ArchivoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
