package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.ObraLiterariaServicio;
import co.edu.uniquindio.proyecto.servicios.PublicacionServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Consultas públicas de publicaciones: listado, búsqueda, detalle y autores. */
@SpringBootTest
@Transactional
@Sql(scripts = "classpath:pli.sql", config = @SqlConfig(encoding = "UTF-8"))
public class PublicacionServicioTest {

    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private ObraLiterariaServicio obraLiterariaServicio;

    private static List<Long> ids(List<Publicacion> publicaciones) {
        return publicaciones.stream().map(Publicacion::getId).collect(Collectors.toList());
    }

    @Test
    public void soloSeListanLasPublicadasDeLaMasRecienteALaMasAntigua() {
        // En pli.sql las publicaciones 2 y 4 están rechazadas
        assertEquals(List.of(5L, 3L, 1L), ids(publicacionServicio.listarPublicacionesPublicadas()));
        assertEquals(5, publicacionServicio.listarPublicaciones().size());
    }

    @Test
    public void laBusquedaNoDistingueMayusculasNiEspaciosYOmiteLasNoPublicadas() {
        assertEquals(List.of(1L), ids(publicacionServicio.buscarPublicacionPorFrase("  Publicación UNO ")));
        // "Dos" está rechazada: no aparece aunque coincida
        assertTrue(publicacionServicio.buscarPublicacionPorFrase("dos").isEmpty());
        // Busca también en el contenido
        assertEquals(3, publicacionServicio.buscarPublicacionPorFrase("contenido de la").size());
        // Sin frase devuelve todas las publicadas
        assertEquals(List.of(5L, 3L, 1L), ids(publicacionServicio.buscarPublicacionPorFrase("  ")));
        assertEquals(List.of(5L, 3L, 1L), ids(publicacionServicio.buscarPublicacionPorFrase(null)));
    }

    @Test
    public void obtenerPublicacionYSusAutores() throws Exception {
        Publicacion publicacion = publicacionServicio.obtenerPublicacion(1L);
        assertEquals("Publicación Uno", publicacion.getTitulo(), "Las tildes de pli.sql deben llegar intactas");

        List<Escritor> autores = publicacionServicio.obtenerEscritoresDeObra(publicacion.getObraLiteraria().getId());
        assertEquals(1, autores.size());
        assertEquals("writer1@example.com", autores.get(0).getEmail());
        assertEquals(autores.get(0).getId(), publicacionServicio.obtenerEscritorDePublicacion(1L).getId());

        Exception e = assertThrows(Exception.class, () -> publicacionServicio.obtenerPublicacion(999L));
        assertEquals("No existe una publicación con ese id", e.getMessage());
        assertThrows(Exception.class, () -> publicacionServicio.obtenerEscritorDePublicacion(999L));
        assertTrue(publicacionServicio.obtenerEscritoresDeObra(999L).isEmpty());
    }

    @Test
    public void registrarActualizarYEliminar() throws Exception {
        Publicacion nueva = new Publicacion();
        nueva.setTitulo("Publicación de prueba");
        nueva.setContenido("Contenido nuevo");
        nueva.setEstado(Estado.PUBLICADO);
        nueva.setFechaPublicacion(new Date());
        nueva.setUrlImagen("");
        nueva.setObraLiteraria(obraLiterariaServicio.obtenerObraLiteraria(3L));

        // Una publicación nueva no tiene id: antes esto lanzaba IllegalArgumentException
        Publicacion guardada = publicacionServicio.registrarPublicacion(nueva);
        assertNotNull(guardada.getId());
        assertEquals(guardada.getId(), ids(publicacionServicio.listarPublicacionesPublicadas()).get(0));

        Publicacion repetida = new Publicacion();
        repetida.setId(guardada.getId());
        Exception e = assertThrows(Exception.class, () -> publicacionServicio.registrarPublicacion(repetida));
        assertEquals("Ya existe una publicación con ese id", e.getMessage());

        guardada.setTitulo("Título cambiado");
        publicacionServicio.actualizarPublicacion(guardada);
        assertEquals("Título cambiado", publicacionServicio.obtenerPublicacion(guardada.getId()).getTitulo());

        publicacionServicio.eliminarPublicacion(guardada.getId());
        assertThrows(Exception.class, () -> publicacionServicio.obtenerPublicacion(guardada.getId()));
        assertThrows(Exception.class, () -> publicacionServicio.eliminarPublicacion(guardada.getId()));
    }
}
