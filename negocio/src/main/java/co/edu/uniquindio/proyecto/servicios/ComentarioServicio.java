package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;

public interface ComentarioServicio {
    Comentario registrarComentario(Comentario comentario) throws Exception;

    Comentario actualizarComentario(Comentario comentario) throws Exception;

    Comentario obtenerComentario(Long idComentario) throws Exception;

    void eliminarComentario(Long idComentario) throws Exception;

    List<Comentario> listarComentario();
}
