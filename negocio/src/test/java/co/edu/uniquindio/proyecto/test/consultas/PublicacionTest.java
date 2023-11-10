package co.edu.uniquindio.proyecto.test.consultas;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PublicacionTest {

    @Autowired
    private PublicacionRepo publicacionRepo;

    @Autowired
    private ObraLiterariaRepo obraLiterariaRepo;

    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarPublicacion() {
        // Crear una nueva publicación y guardarla en la base de datos
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);

        // Asociar la publicación a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        publicacion.setObraLiteraria(obraLiteraria);

        Publicacion publicacionGuardada = publicacionRepo.save(publicacion);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(publicacionGuardada.getId());
        Assertions.assertEquals("Contenido de la publicación", publicacionGuardada.getContenido());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarPublicacionPorId() {
        // Crear una publicación de ejemplo y persistirla en la base de datos
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);

        // Asociar la publicación a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        publicacion.setObraLiteraria(obraLiteraria);
        publicacionRepo.save(publicacion);

        // Buscar la publicación por su ID
        Long idDeLaPublicacion = publicacion.getId();
        Publicacion publicacionEncontrada = publicacionRepo.findById(idDeLaPublicacion).orElse(null);

        // Verificar que se haya encontrado la publicación
        Assertions.assertNotNull(publicacionEncontrada);
        Assertions.assertEquals("Contenido de la publicación", publicacionEncontrada.getContenido());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarPublicacion() {
        // Crear una publicación de ejemplo y persistirla en la base de datos
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);

        // Asociar la publicación a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        publicacion.setObraLiteraria(obraLiteraria);
        publicacionRepo.save(publicacion);

        // Actualizar el contenido de la publicación
        publicacion.setContenido("Nuevo contenido de la publicación");
        publicacionRepo.save(publicacion);

        // Buscar la publicación por su ID después de la actualización
        Long idDeLaPublicacion = publicacion.getId();
        Publicacion publicacionActualizada = publicacionRepo.findById(idDeLaPublicacion).orElse(null);

        // Verificar que se haya actualizado el contenido
        Assertions.assertNotNull(publicacionActualizada);
        Assertions.assertEquals("Nuevo contenido de la publicación", publicacionActualizada.getContenido());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarPublicacion() {
        // Crear una publicación de ejemplo y persistirla en la base de datos
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);

        // Asociar la publicación a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        publicacion.setObraLiteraria(obraLiteraria);
        publicacionRepo.save(publicacion);

        // Eliminar la publicación de la base de datos
        publicacionRepo.delete(publicacion);

        // Intentar buscar la publicación por su ID después de la eliminación
        Long idDeLaPublicacion = publicacion.getId();
        Publicacion publicacionEliminada = publicacionRepo.findById(idDeLaPublicacion).orElse(null);

        // Verificar que la publicación haya sido eliminada (debería ser nula)
        Assertions.assertNull(publicacionEliminada);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarPublicacionesTest(){
        //Obtenemos la lista de todas las publicaciones
        List<Publicacion> lista = publicacionRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }

}
