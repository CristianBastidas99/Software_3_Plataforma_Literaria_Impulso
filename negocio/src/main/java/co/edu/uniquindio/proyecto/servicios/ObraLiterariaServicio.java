package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;
public interface ObraLiterariaServicio {

    ObraLiteraria registrarObraLiteraria(ObraLiteraria obraLiteraria) throws Exception;

    ObraLiteraria actualizarObraLiteraria(ObraLiteraria obraLiteraria) throws Exception;

    ObraLiteraria obtenerObraLiteraria(Long idObraLiteraria) throws Exception;

    void eliminarObraLiteraria(Long idObraLiteraria) throws Exception;

    List<ObraLiteraria> listarObrasLiterarias();

}
