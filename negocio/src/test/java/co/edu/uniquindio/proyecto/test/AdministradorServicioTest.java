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

import java.util.List;

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

    @Test
    @Sql("classpath:pli.sql")
    public void testObtenerAdministradorPorId() {
        // Crear un administrador de ejemplo y persistirlo en la base de datos
        Administrador administrador = new Administrador();
        administrador.setNombre("Admin1");
        administrador.setEmail("admin@example.com");
        administrador.setPassword("admin123");
        administrador.setEstado(Estado.ACTIVO);

        try {
            administradorServicio.registrarAdministrador(administrador);

            // Buscar el administrador por su ID
            Long idDelAdmin = administrador.getId();
            System.out.println(idDelAdmin);
            Administrador adminEncontrado = administradorServicio.obtenerAdministrador(idDelAdmin);

            // Verificar que se haya encontrado el administrador
            Assertions.assertNotNull(adminEncontrado);
            Assertions.assertEquals("Admin1", adminEncontrado.getNombre());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        try {
            administradorServicio.registrarAdministrador(administrador);

            // Actualizar el nombre del administrador
            administrador.setNombre("Nuevo nombre del admin");
            administradorServicio.actualizarAdministrador(administrador);

            // Buscar el administrador por su ID después de la actualización
            Long idDelAdmin = administrador.getId();
            Administrador adminActualizado = administradorServicio.obtenerAdministrador(idDelAdmin);

            // Verificar que se haya actualizado el nombre
            Assertions.assertNotNull(adminActualizado);
            Assertions.assertEquals("Nuevo nombre del admin", adminActualizado.getNombre());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        try {
            administradorServicio.registrarAdministrador(administrador);

            // Eliminar el administrador de la base de datos
            administradorServicio.eliminarAdministrador(administrador.getId());

            // Intentar buscar el administrador por su ID después de la eliminación
            Long idDelAdmin = administrador.getId();
            // Utilizar assertThrows para verificar que el método lanza la excepción esperada
            Exception exception = Assertions.assertThrows(Exception.class, () -> {
                Administrador adminEliminado = administradorServicio.obtenerAdministrador(idDelAdmin);
            });

            // Verificar que el mensaje de la excepción sea el esperado
            String mensajeEsperado = "No exite un administrador con ese id";
            Assertions.assertEquals(mensajeEsperado, exception.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarAdministradoresTest(){
        //Obtenemos la lista de todos los administradores
        List<Administrador> lista = administradorServicio.listarAdministradores();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testValidaLogin() {
        // Datos de prueba
        String email = "admin1@example.com";
        String contrasenaCorrecta = "password123";
        String contrasenaIncorrecta = "contrasenaIncorrecta";

        try {
            // Intento de iniciar sesión con credenciales correctas
            Administrador adminValido = administradorServicio.validaLogin(email, contrasenaCorrecta);

            // Verificar que se haya obtenido el administrador correctamente
            Assertions.assertNotNull(adminValido);
            Assertions.assertEquals(email, adminValido.getEmail());

            // Intento de iniciar sesión con credenciales incorrectas
            Exception exception = Assertions.assertThrows(Exception.class, () -> {
                administradorServicio.validaLogin(email, contrasenaIncorrecta);
            });

            // Verificar que se haya lanzado la excepción esperada
            String mensajeEsperado = "No existe un administrador con ese email y contraseña";
            Assertions.assertEquals(mensajeEsperado, exception.getMessage());

            // Intento de iniciar sesión con administrador inactivo
            // (Asumiendo que hay un administrador inactivo en el archivo de prueba SQL)
            Exception excepcionInactivo = Assertions.assertThrows(Exception.class, () -> {
                administradorServicio.validaLogin("admin3@example.com", "adminpass");
            });

            // Verificar que se haya lanzado la excepción esperada para el administrador inactivo
            String mensajeInactivoEsperado = "El administrador está INACTIVO";
            Assertions.assertEquals(mensajeInactivoEsperado, excepcionInactivo.getMessage());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
