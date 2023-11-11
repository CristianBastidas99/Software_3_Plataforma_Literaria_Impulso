package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;
public interface GeneroServicio {

    Genero registrarGenero(Genero genero) throws Exception;

    Genero actualizarGenero(Genero genero) throws Exception;

    Genero obtenerGenero(Long idGenero) throws Exception;

    void eliminarGenero(Long idGenero) throws Exception;

    List<Genero> listarGeneros();

}
