package co.edu.uniquindio.proyecto.test.consultas;

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
public class GeneroTest {

    @Autowired
    private GeneroRepo generoRepo;


    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarGenero() {
        // Crear un nuevo genero y guardarlo en la base de datos
        Genero genero = new Genero();
        genero.setNombre("Genero1");

        Genero generoGuardado = generoRepo.save(genero);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(generoGuardado.getId());
        Assertions.assertEquals("Genero1", generoGuardado.getNombre());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarGeneroPorId() {
        // Crear un genero de ejemplo y persistirlo en la base de datos
        Genero genero = new Genero();
        genero.setNombre("Genero1");
        generoRepo.save(genero);

        // Buscar el genero por su ID
        Long idDelGenero = genero.getId();
        Genero generoEncontrado = generoRepo.findById(idDelGenero).orElse(null);

        // Verificar que se haya encontrado el genero
        Assertions.assertNotNull(generoEncontrado);
        Assertions.assertEquals("Genero1", generoEncontrado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarGenero() {
        // Crear un genero de ejemplo y persistirlo en la base de datos
        Genero genero = new Genero();
        genero.setNombre("Genero1");
        generoRepo.save(genero);

        // Actualizar el nombre del genero
        genero.setNombre("Nuevo nombre del genero");
        generoRepo.save(genero);

        // Buscar el genero por su ID después de la actualización
        Long idDelGenero = genero.getId();
        Genero generoActualizado = generoRepo.findById(idDelGenero).orElse(null);

        // Verificar que se haya actualizado el nombre
        Assertions.assertNotNull(generoActualizado);
        Assertions.assertEquals("Nuevo nombre del genero", generoActualizado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarGenero() {
        // Crear un genero de ejemplo y persistirlo en la base de datos
        Genero genero = new Genero();
        genero.setNombre("Genero1");
        generoRepo.save(genero);

        // Eliminar el genero de la base de datos
        generoRepo.delete(genero);

        // Intentar buscar el genero por su ID después de la eliminación
        Long idDelGenero = genero.getId();
        Genero generoEliminado = generoRepo.findById(idDelGenero).orElse(null);

        // Verificar que el genero haya sido eliminado (debería ser nulo)
        Assertions.assertNull(generoEliminado);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarGenerosTest(){
        //Obtenemos la lista de todos los generos
        List<Genero> lista = generoRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }


}
