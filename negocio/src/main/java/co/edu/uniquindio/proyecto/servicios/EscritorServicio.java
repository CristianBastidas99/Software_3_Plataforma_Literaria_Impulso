package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;

public interface EscritorServicio {

    Escritor registrarEscritor(Escritor escritor) throws Exception;
    Escritor actualizarEscritor(Escritor escritor) throws Exception;
    Escritor obtenerEscritor(Long idEscritor) throws Exception;
    void eliminarEscritor(Long idEscritor) throws Exception;
    List<Escritor> listarEscritores();
    List<Escritor> buscarEscritoresPorFrase(String frase);
    List<Publicacion> obtenerPublicacionesDeEscritor(Long idEscritor) throws Exception;
    Escritor validaLogin(String email, String contrasena) throws  Exception;

}
