package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.Estadisticas;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.repositorios.ObraLiterariaRepo;
import co.edu.uniquindio.proyecto.repositorios.PublicacionRepo;
import co.edu.uniquindio.proyecto.servicios.AdministracionServicio;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.GeneroServicio;
import co.edu.uniquindio.proyecto.servicios.GestionObraServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.LectorServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del panel de administración sobre {@code pli.sql}: la publicación 1 (obra 1, género 1,
 * escritor 1) está publicada, tiene comentarios y la vio y recomendó el lector 1.
 */
@SpringBootTest
@Transactional
@Sql("classpath:pli.sql")
public class AdministracionServicioTest {

    private static final String BLOQUEADA = "Tu cuenta está bloqueada. Si crees que es un error, escribe al administrador de la plataforma.";

    @Autowired
    private AdministracionServicio admin;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private LectorServicio lectorServicio;
    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private GestionObraServicio gestionObraServicio;
    @Autowired
    private InteraccionServicio interaccion;
    @Autowired
    private GeneroServicio generoServicio;
    @Autowired
    private PublicacionRepo publicacionRepo;
    @Autowired
    private ObraLiterariaRepo obraRepo;
    @Autowired
    private ComentarioRepo comentarioRepo;
    @Autowired
    private LectorRepo lectorRepo;
    @Autowired
    private EscritorRepo escritorRepo;

    private static List<Long> ids(List<Publicacion> publicaciones) {
        return publicaciones.stream().map(Publicacion::getId).collect(Collectors.toList());
    }

    @Test
    public void bloquearImpideIniciarSesion() throws Exception {
        admin.cambiarEstadoEscritor(1L, false);
        Exception e = assertThrows(Exception.class, () -> escritorServicio.validaLogin("writer1@example.com", "writerpass"));
        assertEquals(BLOQUEADA, e.getMessage());
        admin.cambiarEstadoEscritor(1L, true);
        assertNotNull(escritorServicio.validaLogin("writer1@example.com", "writerpass"));

        admin.cambiarEstadoLector(1L, false);
        e = assertThrows(Exception.class, () -> lectorServicio.validaLogin("reader1@example.com", "readerpass"));
        assertEquals(BLOQUEADA, e.getMessage());
        admin.cambiarEstadoLector(1L, true);
        assertNotNull(lectorServicio.validaLogin("reader1@example.com", "readerpass"));

        assertThrows(Exception.class, () -> admin.cambiarEstadoLector(999L, false));
    }

    @Test
    public void ocultarYMostrarPublicacion() throws Exception {
        admin.ocultarPublicacion(1L);
        Publicacion oculta = publicacionRepo.findById(1L).orElseThrow();
        assertEquals(Estado.RECHAZADO, oculta.getEstado());
        assertEquals(Estado.RECHAZADO, oculta.getObraLiteraria().getEstado());
        assertFalse(ids(publicacionServicio.listarPublicacionesPublicadas()).contains(1L));
        assertFalse(ids(interaccion.buscarPublicaciones(null, null, InteraccionServicio.Orden.RECIENTES)).contains(1L));
        // El autor no puede volver a publicar lo que ocultó el administrador
        assertThrows(Exception.class, () -> gestionObraServicio.volverAPublicarObra(1L, 1L));
        assertThrows(Exception.class, () -> admin.ocultarPublicacion(1L));

        admin.mostrarPublicacion(1L);
        assertTrue(ids(publicacionServicio.listarPublicacionesPublicadas()).contains(1L));
        assertThrows(Exception.class, () -> admin.mostrarPublicacion(1L));
    }

    @Test
    public void eliminarPublicacionBorraTodoLoQueDepende() throws Exception {
        assertFalse(comentarioRepo.buscarComentariosEnPublicacion(1L).isEmpty());

        List<String> archivos = admin.eliminarPublicacion(1L);

        assertEquals(List.of("www.imagen1.com", "www.fragmento1.com"), archivos);
        assertFalse(publicacionRepo.existsById(1L));
        assertFalse(obraRepo.existsById(1L));
        assertTrue(comentarioRepo.buscarComentariosEnPublicacion(1L).isEmpty());
        assertFalse(lectorRepo.idsPublicacionesVistas(1L).contains(1L));
        assertEquals(0, interaccion.contarRecomendaciones(1L));
        // El género y el escritor siguen existiendo
        assertNotNull(generoServicio.obtenerGenero(1L));
        assertTrue(escritorRepo.existsById(1L));

        assertThrows(Exception.class, () -> admin.eliminarPublicacion(1L));
    }

    @Test
    public void moderarComentarios() throws Exception {
        Comentario c = interaccion.comentar(1L, lectorRepo.findById(2L).orElseThrow(), "Comentario a moderar");
        admin.ocultarComentario(c.getId());
        assertTrue(interaccion.listarComentarios(1L).stream().noneMatch(x -> x.getId().equals(c.getId())));
        assertTrue(admin.listarComentarios().stream().anyMatch(x -> x.getId().equals(c.getId())));

        admin.mostrarComentario(c.getId());
        assertTrue(interaccion.listarComentarios(1L).stream().anyMatch(x -> x.getId().equals(c.getId())));

        admin.eliminarComentario(c.getId());
        assertFalse(comentarioRepo.existsById(c.getId()));
    }

    @Test
    public void crudDeGeneros() throws Exception {
        Genero poesia = admin.crearGenero("  Poesía   épica ");
        assertEquals("Poesía épica", poesia.getNombre());
        // Aparece en la lista que usa el formulario de nueva obra
        assertTrue(generoServicio.listarGeneros().stream().anyMatch(g -> g.getId().equals(poesia.getId())));

        Exception e = assertThrows(Exception.class, () -> admin.crearGenero("POESÍA ÉPICA"));
        assertTrue(e.getMessage().startsWith("Ya existe un género"));
        assertThrows(Exception.class, () -> admin.crearGenero("   "));
        assertThrows(Exception.class, () -> admin.crearGenero("x".repeat(AdministracionServicio.GENERO_MAX + 1)));

        assertEquals("Poesía lírica", admin.renombrarGenero(poesia.getId(), "Poesía lírica").getNombre());
        // Renombrar con su propio nombre en otras mayúsculas está permitido
        admin.renombrarGenero(poesia.getId(), "poesía lírica");

        // Un género con obras no se puede eliminar
        e = assertThrows(Exception.class, () -> admin.eliminarGenero(1L));
        assertTrue(e.getMessage().contains("obra"));

        // Un género sin obras sí, y desaparece de las preferencias de los lectores
        interaccion.actualizarGenerosPreferidos(2L, List.of(poesia.getId(), 2L));
        admin.eliminarGenero(poesia.getId());
        assertThrows(Exception.class, () -> generoServicio.obtenerGenero(poesia.getId()));
        assertEquals(List.of(2L), interaccion.obtenerGenerosPreferidos(2L));
    }

    @Test
    public void estadisticas() throws Exception {
        admin.cambiarEstadoLector(2L, false);
        admin.ocultarPublicacion(3L);
        Estadisticas e = admin.obtenerEstadisticas(2);

        assertEquals(escritorRepo.count(), e.getEscritores());
        assertEquals(lectorRepo.count(), e.getLectores());
        assertEquals(lectorRepo.countByEstado(Estado.INACTIVO), e.getLectoresBloqueados());
        assertEquals(obraRepo.count(), e.getObras());
        assertEquals(publicacionRepo.countByEstado(Estado.RECHAZADO), e.getPublicacionesOcultas());
        assertTrue(e.getPublicacionesOcultas() >= 1);
        assertEquals(comentarioRepo.count(), e.getComentarios());
        assertTrue(e.getMasVistas().size() <= 2);
        assertTrue(e.getMasVistas().stream().noneMatch(p -> p.getId().equals(3L)));
        if (e.getMasVistas().size() == 2) {
            assertTrue(e.getMasVistas().get(0).getVistas() >= e.getMasVistas().get(1).getVistas());
        }
    }
}
