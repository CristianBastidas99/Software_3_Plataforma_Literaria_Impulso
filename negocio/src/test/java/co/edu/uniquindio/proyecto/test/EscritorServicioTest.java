package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.*;
import co.edu.uniquindio.proyecto.servicios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class EscritorServicioTest {

    @Autowired
    private EscritorServicio escritorServicio;

    @Test
    //@Sql("classpath:pli.sql")
    public void obtenerPublicacionesAprobadasDeEscritorTest(){
        //Obtenemos la lista de todos los administradores
        Long id = Long.valueOf("1");
        List<Publicacion> lista = null;
        try {
            lista = escritorServicio.obtenerPublicacionesDeEscritor(id);
            //Imprimimos la lista
            System.out.println(lista);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    //@Sql("classpath:pli.sql")
    public void obtenerEscritoresPorFraseTest(){
        //Obtenemos la lista de todos los administradores
        String frase = "Es";
        List<Escritor> lista = null;
        try {
            lista = escritorServicio.buscarEscritoresPorFrase(frase);
            //Imprimimos la lista
            System.out.println(lista);
            lista = escritorServicio.listarEscritores();
            //Imprimimos la lista
            System.out.println(lista);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
