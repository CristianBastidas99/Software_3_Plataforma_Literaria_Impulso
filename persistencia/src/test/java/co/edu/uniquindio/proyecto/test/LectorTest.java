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
public class LectorTest {

    @Autowired
    private LectorRepo lectorRepo;

    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarLector() {
        // Crear un nuevo lector y guardarlo en la base de datos
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("lector@example.com");
        lector.setPassword("lector123");
        lector.setEstado(Estado.ACTIVO);

        Lector lectorGuardado = lectorRepo.save(lector);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(lectorGuardado.getId());
        Assertions.assertEquals("Lector1", lectorGuardado.getNombre());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarLectorPorId() {
        // Crear un lector de ejemplo y persistirlo en la base de datos
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("lector@example.com");
        lector.setPassword("lector123");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        // Buscar el lector por su ID
        Long idDelLector = lector.getId();
        Lector lectorEncontrado = lectorRepo.findById(idDelLector).orElse(null);

        // Verificar que se haya encontrado el lector
        Assertions.assertNotNull(lectorEncontrado);
        Assertions.assertEquals("Lector1", lectorEncontrado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarLector() {
        // Crear un lector de ejemplo y persistirlo en la base de datos
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("lector@example.com");
        lector.setPassword("lector123");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        // Actualizar el nombre del lector
        lector.setNombre("Nuevo nombre del lector");
        lectorRepo.save(lector);

        // Buscar el lector por su ID después de la actualización
        Long idDelLector = lector.getId();
        Lector lectorActualizado = lectorRepo.findById(idDelLector).orElse(null);

        // Verificar que se haya actualizado el nombre
        Assertions.assertNotNull(lectorActualizado);
        Assertions.assertEquals("Nuevo nombre del lector", lectorActualizado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarLector() {
        // Crear un lector de ejemplo y persistirlo en la base de datos
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("lector@example.com");
        lector.setPassword("lector123");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        // Eliminar el lector de la base de datos
        lectorRepo.delete(lector);

        // Intentar buscar el lector por su ID después de la eliminación
        Long idDelLector = lector.getId();
        Lector lectorEliminado = lectorRepo.findById(idDelLector).orElse(null);

        // Verificar que el lector haya sido eliminado (debería ser nulo)
        Assertions.assertNull(lectorEliminado);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarLectoresTest(){
        //Obtenemos la lista de todos los lectores
        List<Lector> lista = lectorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }


}
