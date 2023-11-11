package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;

public interface AdministradorServicio {
    Administrador registrarAdministrador(Administrador administrador) throws Exception;

    Administrador actualizarAdministrador(Administrador administrador) throws Exception;

    Administrador obtenerAdministrador(Long idAdministrador) throws Exception;

    void eliminarAdministrador(Long idAdministrador) throws Exception;

    List<Administrador> listarAdministradores();

    Administrador validaLogin(String email, String constrasena) throws  Exception;
}
