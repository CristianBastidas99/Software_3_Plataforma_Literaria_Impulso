package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;
public interface PublicacionServicio {

    Publicacion registrarPublicacion(Publicacion publicacion) throws Exception;
    Publicacion actualizarPublicacion(Publicacion publicacion) throws Exception;
    Publicacion obtenerPublicacion(Long idPublicacion) throws Exception;
    void eliminarPublicacion(Long idPublicacion) throws Exception;
    List<Publicacion> listarPublicaciones();
    List<Publicacion> buscarPublicacionPorFrase(String frase);

}
