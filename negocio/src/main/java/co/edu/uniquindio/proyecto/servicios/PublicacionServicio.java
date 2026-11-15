package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;
public interface PublicacionServicio {

    Publicacion registrarPublicacion(Publicacion publicacion) throws Exception;
    Publicacion actualizarPublicacion(Publicacion publicacion) throws Exception;
    Publicacion obtenerPublicacion(Long idPublicacion) throws Exception;
    void eliminarPublicacion(Long idPublicacion) throws Exception;
    List<Publicacion> listarPublicaciones();

    /** Publicaciones visibles para el público (estado PUBLICADO), de la más reciente a la más antigua. */
    List<Publicacion> listarPublicacionesPublicadas();
    List<Publicacion> buscarPublicacionPorFrase(String frase);
    Escritor obtenerEscritorDePublicacion(Long idObraLiteraria) throws Exception;

    /** Autores y coautores de una obra. */
    List<Escritor> obtenerEscritoresDeObra(Long idObraLiteraria);
}
