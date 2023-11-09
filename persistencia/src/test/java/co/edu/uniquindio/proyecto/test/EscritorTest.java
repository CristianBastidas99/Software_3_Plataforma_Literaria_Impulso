package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EscritorTest {

    @Autowired
    private EscritorRepo escritorRepo;


    @Test
    public void testGuardarEscritor() {
        // Crear un nuevo escritor y guardarlo en la base de datos
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);

        Escritor escritorGuardado = escritorRepo.save(escritor);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(escritorGuardado.getId());
        Assertions.assertEquals("Escritor1", escritorGuardado.getNombre());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    public void testBuscarEscritorPorId() {
        // Crear un escritor de ejemplo y persistirlo en la base de datos
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        // Buscar el escritor por su ID
        Long idDelEscritor = escritor.getId();
        Escritor escritorEncontrado = escritorRepo.findById(idDelEscritor).orElse(null);
        System.out.println(escritorEncontrado.toString());

        // Verificar que se haya encontrado el escritor
        Assertions.assertNotNull(escritorEncontrado);
        Assertions.assertEquals("Escritor1", escritorEncontrado.getNombre());
    }

    @Test
    public void testActualizarEscritor() {
        // Crear un escritor de ejemplo y persistirlo en la base de datos
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        // Actualizar el nombre del escritor
        escritor.setNombre("Nuevo nombre del escritor");
        escritorRepo.save(escritor);

        // Buscar el escritor por su ID después de la actualización
        Long idDelEscritor = escritor.getId();
        Escritor escritorActualizado = escritorRepo.findById(idDelEscritor).orElse(null);

        // Verificar que se haya actualizado el nombre
        Assertions.assertNotNull(escritorActualizado);
        Assertions.assertEquals("Nuevo nombre del escritor", escritorActualizado.getNombre());
    }

    @Test
    public void testEliminarEscritor() {
        // Crear un escritor de ejemplo y persistirlo en la base de datos
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        // Eliminar el escritor de la base de datos
        escritorRepo.delete(escritor);

        // Intentar buscar el escritor por su ID después de la eliminación
        Long idDelEscritor = escritor.getId();
        Escritor escritorEliminado = escritorRepo.findById(idDelEscritor).orElse(null);

        // Verificar que el escritor haya sido eliminado (debería ser nulo)
        Assertions.assertNull(escritorEliminado);
    }

    @Test
    @Sql("classpath:pli.sql" )
    public void listarUsuariosTest(){
        //Obtenemos la lista de todos los usuarios
        List<Escritor> lista = escritorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
