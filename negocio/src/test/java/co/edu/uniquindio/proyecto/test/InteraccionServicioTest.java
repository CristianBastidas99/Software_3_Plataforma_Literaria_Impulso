package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio;
import co.edu.uniquindio.proyecto.servicios.InteraccionServicio.Orden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la interacción con publicaciones y escritores. En {@code pli.sql} las publicaciones
 * 1, 3 y 5 están publicadas y la 2 rechazada; el lector 1 ya vio y recomendó la 1 y sigue al
 * escritor 1; la obra 1 es del género 1 y del escritor 1.
 */
@SpringBootTest
@Transactional
@Sql("classpath:pli.sql")
public class InteraccionServicioTest {

    @Autowired
    private InteraccionServicio interaccion;
    @Autowired
    private LectorRepo lectorRepo;
    @Autowired
    private EscritorRepo escritorRepo;
    @Autowired
    private ComentarioRepo comentarioRepo;

    private Lector lector(long id) {
        return lectorRepo.findById(id).orElseThrow();
    }

    private static List<Long> ids(List<Publicacion> publicaciones) {
        return publicaciones.stream().map(Publicacion::getId).collect(Collectors.toList());
    }

    @Test
    public void comentarCensuraYApareceElPrimero() throws Exception {
        Comentario c = interaccion.comentar(1L, lector(2), "  Qué final tan BOBALICÓN  ");
        assertEquals("Qué final tan *********", c.getContenido());
        assertNotNull(c.getFechaPublicacion());
        assertEquals(c.getId(), interaccion.listarComentarios(1L).get(0).getId());

        Escritor escritor = escritorRepo.findById(2L).orElseThrow();
        Comentario deEscritor = interaccion.comentar(1L, escritor, "Gracias por leer");
        assertNull(deEscritor.getLector());
        assertTrue(interaccion.esAutorDeComentario(deEscritor, escritor));
    }

    @Test
    public void comentariosInvalidos() {
        long antes = comentarioRepo.count();
        assertThrows(Exception.class, () -> interaccion.comentar(1L, lector(2), "   "));
        assertThrows(Exception.class, () -> interaccion.comentar(1L, lector(2), "x".repeat(InteraccionServicio.COMENTARIO_MAX + 1)));
        assertThrows(Exception.class, () -> interaccion.comentar(2L, lector(2), "Publicación rechazada"));
        assertThrows(Exception.class, () -> interaccion.comentar(999L, lector(2), "No existe"));
        Administrador admin = new Administrador();
        admin.setId(1L);
        assertThrows(Exception.class, () -> interaccion.comentar(1L, admin, "Soy admin"));
        assertThrows(Exception.class, () -> interaccion.comentar(1L, null, "Sin sesión"));
        assertEquals(antes, comentarioRepo.count());
    }

    @Test
    public void soloElAutorEliminaSuComentario() throws Exception {
        Comentario c = interaccion.comentar(1L, lector(2), "Comentario para borrar");
        Exception e = assertThrows(Exception.class, () -> interaccion.eliminarComentario(c.getId(), lector(4)));
        assertEquals("Solo puedes eliminar tus propios comentarios", e.getMessage());
        // Un escritor con el mismo id que el lector tampoco puede
        assertThrows(Exception.class, () -> interaccion.eliminarComentario(c.getId(), escritorRepo.findById(2L).orElseThrow()));
        assertTrue(comentarioRepo.existsById(c.getId()));

        interaccion.eliminarComentario(c.getId(), lector(2));
        assertFalse(comentarioRepo.existsById(c.getId()));
    }

    @Test
    public void recomendarActivaYDesactiva() throws Exception {
        long antes = interaccion.contarRecomendaciones(1L);
        assertFalse(interaccion.haRecomendado(2L, 1L));

        assertTrue(interaccion.alternarRecomendacion(2L, 1L));
        assertTrue(interaccion.haRecomendado(2L, 1L));
        assertEquals(antes + 1, interaccion.contarRecomendaciones(1L));

        assertFalse(interaccion.alternarRecomendacion(2L, 1L));
        assertFalse(interaccion.haRecomendado(2L, 1L));
        assertEquals(antes, interaccion.contarRecomendaciones(1L));

        assertThrows(Exception.class, () -> interaccion.alternarRecomendacion(2L, 2L));
    }

    @Test
    public void vistaUnaVezPorLector() {
        long antes = interaccion.contarVistas(3L);
        interaccion.registrarVista(2L, 3L);
        interaccion.registrarVista(2L, 3L);
        interaccion.registrarVista(2L, 3L);
        assertEquals(antes + 1, interaccion.contarVistas(3L));

        // El lector 1 ya había visto la publicación 1
        long vistas1 = interaccion.contarVistas(1L);
        interaccion.registrarVista(1L, 1L);
        assertEquals(vistas1, interaccion.contarVistas(1L));

        // Una publicación rechazada no suma vistas
        long vistas2 = interaccion.contarVistas(2L);
        interaccion.registrarVista(4L, 2L);
        assertEquals(vistas2, interaccion.contarVistas(2L));
    }

    @Test
    public void escritorFavorito() throws Exception {
        long antes = interaccion.contarSeguidores(1L);
        assertTrue(interaccion.alternarFavorito(2L, 1L));
        assertTrue(interaccion.esFavorito(2L, 1L));
        assertEquals(antes + 1, interaccion.contarSeguidores(1L));
        assertFalse(interaccion.alternarFavorito(2L, 1L));
        assertEquals(antes, interaccion.contarSeguidores(1L));
        assertThrows(Exception.class, () -> interaccion.alternarFavorito(2L, 999L));
    }

    @Test
    public void generosPreferidosYRecomendadasParaTi() throws Exception {
        // El lector 2 prefiere el género 2 y sigue al escritor 2, cuyas obras no están publicadas
        assertTrue(interaccion.recomendadasPara(2L, 6).isEmpty());

        interaccion.actualizarGenerosPreferidos(2L, List.of(1L, 3L));
        assertEquals(2, interaccion.obtenerGenerosPreferidos(2L).size());
        List<Long> recomendadas = ids(interaccion.recomendadasPara(2L, 6));
        assertTrue(recomendadas.containsAll(List.of(1L, 3L)));
        assertFalse(recomendadas.contains(2L));

        // Las ya vistas van al final
        interaccion.registrarVista(2L, 1L);
        assertEquals(List.of(3L, 1L), ids(interaccion.recomendadasPara(2L, 6)));

        interaccion.actualizarGenerosPreferidos(2L, List.of());
        assertTrue(interaccion.obtenerGenerosPreferidos(2L).isEmpty());
        assertThrows(Exception.class, () -> interaccion.actualizarGenerosPreferidos(2L, List.of(999L)));
    }

    @Test
    public void buscarConFiltros() throws Exception {
        assertEquals(List.of(5L, 3L, 1L), ids(interaccion.buscarPublicaciones(null, null, Orden.RECIENTES)));
        assertEquals(List.of(1L), ids(interaccion.buscarPublicaciones("UNO", null, Orden.RECIENTES)));
        assertEquals(List.of(3L), ids(interaccion.buscarPublicaciones("", 3L, Orden.RECIENTES)));
        assertEquals(List.of(3L), ids(interaccion.buscarPublicaciones("TRES", 3L, Orden.RECIENTES)));
        assertTrue(interaccion.buscarPublicaciones("uno", 3L, Orden.RECIENTES).isEmpty());
        // Género 2: solo tiene la publicación rechazada
        assertTrue(interaccion.buscarPublicaciones(null, 2L, Orden.RECIENTES).isEmpty());
        // Los comodines que escribe el usuario se buscan literalmente
        assertTrue(interaccion.buscarPublicaciones("100%", null, Orden.RECIENTES).isEmpty());
        assertTrue(interaccion.buscarPublicaciones("_", null, Orden.RECIENTES).isEmpty());

        // Popularidad = vistas + recomendaciones: con una recomendación más, la 3 pasa al primer lugar
        interaccion.alternarRecomendacion(2L, 3L);
        assertEquals(3L, interaccion.buscarPublicaciones(null, null, Orden.POPULARES).get(0).getId());
    }
}
