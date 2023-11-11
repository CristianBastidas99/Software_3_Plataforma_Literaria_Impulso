package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;

import java.util.List;

public interface FragmentoServicio {

    Fragmento registrarFragmento(Fragmento fragmento) throws Exception;

    Fragmento actualizarFragmento(Fragmento fragmento) throws Exception;

    Fragmento obtenerFragmento(Long idFragmento) throws Exception;

    void eliminarFragmento(Long idFragmento) throws Exception;

    List<Fragmento> listarFragmentos();


}
