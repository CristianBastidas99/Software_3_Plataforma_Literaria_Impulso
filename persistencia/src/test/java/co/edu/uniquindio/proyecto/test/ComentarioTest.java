package co.edu.uniquindio.proyecto.test;

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
public class ComentarioTest {

    @Autowired
    private ComentarioRepo comentarioRepo;
    @Autowired
    private PublicacionRepo publicacionRepo;
    @Autowired
    private EscritorRepo escritorRepo;
    @Autowired
    private LectorRepo lectorRepo;

    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarComentario() {
        // Crear un nuevo comentario y guardarlo en la base de datos
        Comentario comentario = new Comentario();
        comentario.setContenido("Contenido del comentario");
        comentario.setFechaPublicacion(new Date());
        comentario.setEstado(Estado.ACTIVO);

        // Asociar el comentario a una publicación existente (puedes crearla o cargarla desde la base de datos)
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);
        publicacionRepo.save(publicacion);

        comentario.setPublicacion(publicacion);

        // Asociar el comentario a un escritor existente (puedes crearlo o cargarlo desde la base de datos)
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        comentario.setEscritor(escritor);

        // Asociar el comentario a un lector existente (puedes crearlo o cargarlo desde la base de datos)
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("correolector@example.com");
        lector.setPassword("67890");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        comentario.setLector(lector);

        Comentario comentarioGuardado = comentarioRepo.save(comentario);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(comentarioGuardado.getId());
        Assertions.assertEquals("Contenido del comentario", comentarioGuardado.getContenido());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarComentarioPorId() {
        // Crear un comentario de ejemplo y persistirlo en la base de datos
        Comentario comentario = new Comentario();
        comentario.setContenido("Contenido del comentario");
        comentario.setFechaPublicacion(new Date());
        comentario.setEstado(Estado.ACTIVO);

        // Asociar el comentario a una publicación existente (puedes crearla o cargarla desde la base de datos)
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);
        publicacionRepo.save(publicacion);

        comentario.setPublicacion(publicacion);

        // Asociar el comentario a un escritor existente (puedes crearlo o cargarlo desde la base de datos)
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        comentario.setEscritor(escritor);

        // Asociar el comentario a un lector existente (puedes crearlo o cargarlo desde la base de datos)
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("correolector@example.com");
        lector.setPassword("67890");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        comentario.setLector(lector);

        comentarioRepo.save(comentario);

        // Buscar el comentario por su ID
        Long idDelComentario = comentario.getId();
        Comentario comentarioEncontrado = comentarioRepo.findById(idDelComentario).orElse(null);

        // Verificar que se haya encontrado el comentario
        Assertions.assertNotNull(comentarioEncontrado);
        Assertions.assertEquals("Contenido del comentario", comentarioEncontrado.getContenido());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarComentario() {
        // Crear un comentario de ejemplo y persistirlo en la base de datos
        Comentario comentario = new Comentario();
        comentario.setContenido("Contenido del comentario");
        comentario.setFechaPublicacion(new Date());
        comentario.setEstado(Estado.ACTIVO);

        // Asociar el comentario a una publicación existente (puedes crearla o cargarla desde la base de datos)
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);
        publicacionRepo.save(publicacion);

        comentario.setPublicacion(publicacion);

        // Asociar el comentario a un escritor existente (puedes crearlo o cargarlo desde la base de datos)
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        comentario.setEscritor(escritor);

        // Asociar el comentario a un lector existente (puedes crearlo o cargarlo desde la base de datos)
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("correolector@example.com");
        lector.setPassword("67890");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        comentario.setLector(lector);

        comentarioRepo.save(comentario);

        // Actualizar el contenido del comentario
        comentario.setContenido("Nuevo contenido del comentario");
        comentarioRepo.save(comentario);

        // Buscar el comentario por su ID después de la actualización
        Long idDelComentario = comentario.getId();
        Comentario comentarioActualizado = comentarioRepo.findById(idDelComentario).orElse(null);

        // Verificar que se haya actualizado el contenido
        Assertions.assertNotNull(comentarioActualizado);
        Assertions.assertEquals("Nuevo contenido del comentario", comentarioActualizado.getContenido());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarComentario() {
        // Crear un comentario de ejemplo y persistirlo en la base de datos
        Comentario comentario = new Comentario();
        comentario.setContenido("Contenido del comentario");
        comentario.setFechaPublicacion(new Date());
        comentario.setEstado(Estado.ACTIVO);

        // Asociar el comentario a una publicación existente (puedes crearla o cargarla desde la base de datos)
        Publicacion publicacion = new Publicacion();
        publicacion.setContenido("Contenido de la publicación");
        publicacion.setFechaPublicacion(new Date());
        publicacion.setTitulo("Publicacion1");
        publicacion.setUrlImagen("https://example.com/imagen1");
        publicacion.setEstado(Estado.ACTIVO);
        publicacionRepo.save(publicacion);

        comentario.setPublicacion(publicacion);

        // Asociar el comentario a un escritor existente (puedes crearlo o cargarlo desde la base de datos)
        Escritor escritor = new Escritor();
        escritor.setNombre("Escritor1");
        escritor.setEmail("correo@example.com");
        escritor.setPassword("12345");
        escritor.setBiografia("Esta es una bibliografia");
        escritor.setEstado(Estado.ACTIVO);
        escritorRepo.save(escritor);

        comentario.setEscritor(escritor);

        // Asociar el comentario a un lector existente (puedes crearlo o cargarlo desde la base de datos)
        Lector lector = new Lector();
        lector.setNombre("Lector1");
        lector.setEmail("correolector@example.com");
        lector.setPassword("67890");
        lector.setEstado(Estado.ACTIVO);
        lectorRepo.save(lector);

        comentario.setLector(lector);

        comentarioRepo.save(comentario);

        // Eliminar el comentario de la base de datos
        comentarioRepo.delete(comentario);

        // Intentar buscar el comentario por su ID después de la eliminación
        Long idDelComentario = comentario.getId();
        Comentario comentarioEliminado = comentarioRepo.findById(idDelComentario).orElse(null);

        // Verificar que el comentario haya sido eliminado (debería ser nulo)
        Assertions.assertNull(comentarioEliminado);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarComentariosTest(){
        //Obtenemos la lista de todos los comentarios
        List<Comentario> lista = comentarioRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }


}
