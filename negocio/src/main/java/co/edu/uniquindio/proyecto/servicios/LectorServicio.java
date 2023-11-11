package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;
public interface LectorServicio {

    Lector registrarLector(Lector lector) throws Exception;

    Lector actualizarLector(Lector lector) throws Exception;

    Lector obtenerLector(Long idLector) throws Exception;

    void eliminarLector(Long idLector) throws Exception;

    List<Lector> listarLectores();

    Lector validaLogin(String email, String contrasena) throws  Exception;

}
