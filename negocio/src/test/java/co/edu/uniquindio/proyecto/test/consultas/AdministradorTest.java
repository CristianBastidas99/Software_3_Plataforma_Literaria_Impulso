package co.edu.uniquindio.proyecto.test.consultas;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AdministradorTest {

    @Autowired
    private AdministradorRepo administradorRepo;

    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarAdministrador() {
        // Crear un nuevo administrador y guardarlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);

        Administrador adminGuardado = administradorRepo.save(administrador);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(adminGuardado.getId());
        Assertions.assertEquals("Admin1", adminGuardado.getNombre());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarAdministradorPorId() {
        // Crear un administrador de ejemplo y persistirlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);
        administradorRepo.save(administrador);

        // Buscar el administrador por su ID
        Long idDelAdmin = administrador.getId();
        Administrador adminEncontrado = administradorRepo.findById(idDelAdmin).orElse(null);

        // Verificar que se haya encontrado el administrador
        Assertions.assertNotNull(adminEncontrado);
        Assertions.assertEquals("Admin1", adminEncontrado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarAdministrador() {
        // Crear un administrador de ejemplo y persistirlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);
        administradorRepo.save(administrador);

        // Actualizar el nombre del administrador
        administrador.setNombre("Nuevo nombre del admin");
        administradorRepo.save(administrador);

        // Buscar el administrador por su ID después de la actualización
        Long idDelAdmin = administrador.getId();
        Administrador adminActualizado = administradorRepo.findById(idDelAdmin).orElse(null);

        // Verificar que se haya actualizado el nombre
        Assertions.assertNotNull(adminActualizado);
        Assertions.assertEquals("Nuevo nombre del admin", adminActualizado.getNombre());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarAdministrador() {
        // Crear un administrador de ejemplo y persistirlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);
        administradorRepo.save(administrador);

        // Eliminar el administrador de la base de datos
        administradorRepo.delete(administrador);

        // Intentar buscar el administrador por su ID después de la eliminación
        Long idDelAdmin = administrador.getId();
        Administrador adminEliminado = administradorRepo.findById(idDelAdmin).orElse(null);

        // Verificar que el administrador haya sido eliminado (debería ser nulo)
        Assertions.assertNull(adminEliminado);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarAdministradoresTest(){
        //Obtenemos la lista de todos los administradores
        List<Administrador> lista = administradorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarAdministradoresActivosTest(){
        //Obtenemos la lista de todos los administradores
        List<Administrador> lista = administradorRepo.obtenerAdministradoresActivos();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarAdministradorPorCorreo() {
        Optional<Administrador> administrador = administradorRepo.buscarAdministradorPorCorreo("admin@example.com");
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(administrador);
        if(administrador.isPresent()) {
            System.out.println(administrador.get());
        }
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testContarAdministradores() {
        Long cantidadAdministradores = administradorRepo.contarAdministradores();
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(cantidadAdministradores);
        System.out.println("Cantidad de administradores: " + cantidadAdministradores);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testListarAdministradoresOrdenadosPorNombre() {
        List<Administrador> administradoresOrdenados = administradorRepo.listarAdministradoresOrdenadosPorNombre();
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(administradoresOrdenados);
        System.out.println(administradoresOrdenados);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testObtenerAdministradoresPorEstado() {
        List<Administrador> administradoresActivos = administradorRepo.obtenerAdministradoresPorEstado(Estado.ACTIVO);
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(administradoresActivos);
        System.out.println(administradoresActivos);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarAdministradoresPorNombre() {
        List<Administrador> administradoresPorNombre = administradorRepo.buscarAdministradoresPorNombre("Admin Uno");
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(administradoresPorNombre.get(0));
        System.out.println(administradoresPorNombre);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testObtenerAdministradoresOrdenadosPorFechaCreacion() {
        List<Administrador> administradoresOrdenadosPorFecha = administradorRepo.obtenerAdministradoresOrdenadosPorFechaCreacion();
        // Puedes agregar aserciones según tus necesidades
        Assertions.assertNotNull(administradoresOrdenadosPorFecha);
        System.out.println(administradoresOrdenadosPorFecha);
    }

}
