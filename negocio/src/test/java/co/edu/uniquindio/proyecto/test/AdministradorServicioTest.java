package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.repositorios.*;
import co.edu.uniquindio.proyecto.servicios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
@SpringBootTest
@Transactional
public class AdministradorServicioTest {

    @Autowired
    private AdministradorServicio administradorServicio;

    @Test
    @Sql("classpath:pli.sql")
    public void testRegistrarAdministrador() {
        // Crear un nuevo administrador y guardarlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);

        Administrador adminRegistrado = null;
        try {
            adminRegistrado = administradorServicio.registrarAdministrador(administrador);
            // Verificar que se haya guardado correctamente
            Assertions.assertNotNull(adminRegistrado.getId());
            Assertions.assertEquals("Admin1", adminRegistrado.getNombre());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



}
