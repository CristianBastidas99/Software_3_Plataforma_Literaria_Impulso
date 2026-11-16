package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.DatosObra;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.ObraLiteraria;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.ObraLiterariaRepo;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.GestionObraServicio;
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

/**
 * Pruebas de la publicación de obras por parte de los escritores. En {@code pli.sql} los escritores
 * 1, 2 y 4 están activos y el 3 inactivo; los géneros 1 y 2 son Ficción y Romance.
 */
@SpringBootTest
@Transactional
@Sql(scripts = "classpath:pli.sql", config = @SqlConfig(encoding = "UTF-8"))
public class GestionObraServicioTest {

    private static final Long AUTOR = 1L;
    private static final Long COAUTOR = 2L;
    private static final Long INACTIVO = 3L;
    private static final Long OTRO = 4L;

    @Autowired
    private GestionObraServicio gestionObraServicio;
    @Autowired
    private PublicacionServicio publicacionServicio;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private ObraLiterariaRepo obraRepo;

    private DatosObra datosValidos() {
        DatosObra datos = new DatosObra();
        datos.setTitulo("El faro de Salento");
        datos.setSinopsis("Un farero sin mar espera barcos en lo alto de la cordillera.");
        datos.setDescripcion("Novela corta sobre la espera y la terquedad.");
        datos.setIsbn("978-958-99-0070-3");
        datos.setEditorial("Ediciones de prueba");
        datos.setFechaPublicacion(new Date());
        datos.setGeneros(List.of(1L, 2L));
        datos.setUrlPortada("uploads/img/portada.png");
        datos.setUrlFragmento("uploads/pdf/fragmento.pdf");
        return datos;
    }

    private static List<Long> ids(List<Publicacion> publicaciones) {
        return publicaciones.stream().map(Publicacion::getId).collect(Collectors.toList());
    }

    @Test
    public void crearObraCompleta() throws Exception {
        DatosObra datos = datosValidos();
        datos.setCoautores(List.of(COAUTOR, AUTOR));

        ObraLiteraria obra = gestionObraServicio.crearObra(AUTOR, datos);

        assertNotNull(obra.getId());
        assertEquals(Estado.PUBLICADO, obra.getEstado());
        assertEquals("uploads/pdf/fragmento.pdf", obra.getFragmento().getUrl());
        Publicacion publicacion = obra.getPublicacion();
        assertNotNull(publicacion.getId());
        assertEquals("El faro de Salento", publicacion.getTitulo());
        assertEquals("uploads/img/portada.png", publicacion.getUrlImagen());
        assertEquals(List.of(AUTOR, COAUTOR),
                obra.getEscritores().stream().map(Escritor::getId).collect(Collectors.toList()));
        assertEquals(2, obra.getGeneros().size());

        // Aparece en inicio, en la búsqueda y en el perfil de los dos autores
        assertTrue(ids(publicacionServicio.listarPublicacionesPublicadas()).contains(publicacion.getId()));
        assertTrue(ids(publicacionServicio.buscarPublicacionPorFrase("faro de salento")).contains(publicacion.getId()));
        assertTrue(ids(escritorServicio.obtenerPublicacionesDeEscritor(AUTOR)).contains(publicacion.getId()));
        assertTrue(ids(escritorServicio.obtenerPublicacionesDeEscritor(COAUTOR)).contains(publicacion.getId()));
        assertTrue(gestionObraServicio.listarObrasDeAutor(COAUTOR).contains(obra));
    }

    @Test
    public void datosInvalidosNoGuardanNada() {
        long antes = obraRepo.count();

        DatosObra sinTitulo = datosValidos();
        sinTitulo.setTitulo("  ");
        Exception e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, sinTitulo));
        assertTrue(e.getMessage().contains("El título es obligatorio"));

        DatosObra isbnMalo = datosValidos();
        isbnMalo.setIsbn("978-958-99-0070-4");
        e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, isbnMalo));
        assertTrue(e.getMessage().contains("ISBN"));

        DatosObra sinGeneros = datosValidos();
        sinGeneros.setGeneros(List.of());
        e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, sinGeneros));
        assertTrue(e.getMessage().contains("género"));

        DatosObra sinPdf = datosValidos();
        sinPdf.setUrlFragmento(null);
        e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, sinPdf));
        assertEquals("El fragmento en PDF es obligatorio", e.getMessage());

        DatosObra sinopsisLarga = datosValidos();
        sinopsisLarga.setSinopsis("s".repeat(DatosObra.SINOPSIS_MAX + 1));
        assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, sinopsisLarga));

        DatosObra coautorInactivo = datosValidos();
        coautorInactivo.setCoautores(List.of(INACTIVO));
        e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(AUTOR, coautorInactivo));
        assertTrue(e.getMessage().contains("no está activo"));

        assertThrows(Exception.class, () -> gestionObraServicio.crearObra(INACTIVO, datosValidos()));

        assertEquals(antes, obraRepo.count());
    }

    @Test
    public void isbnRepetido() throws Exception {
        gestionObraServicio.crearObra(AUTOR, datosValidos());
        DatosObra otra = datosValidos();
        otra.setTitulo("Otra obra");
        Exception e = assertThrows(Exception.class, () -> gestionObraServicio.crearObra(OTRO, otra));
        assertEquals("Ya existe una obra con ese ISBN", e.getMessage());
    }

    @Test
    public void editarObraPropia() throws Exception {
        ObraLiteraria obra = gestionObraServicio.crearObra(AUTOR, datosValidos());

        DatosObra cambios = datosValidos();
        cambios.setTitulo("El faro de Salento (edición revisada)");
        cambios.setDescripcion("Nueva descripción");
        cambios.setGeneros(List.of(2L));
        cambios.setUrlFragmento("uploads/pdf/nuevo.pdf");
        ObraLiteraria editada = gestionObraServicio.actualizarObra(AUTOR, obra.getId(), cambios);

        assertEquals("El faro de Salento (edición revisada)", editada.getTitulo());
        assertEquals("El faro de Salento (edición revisada)", editada.getPublicacion().getTitulo());
        assertEquals("Nueva descripción", editada.getPublicacion().getContenido());
        assertEquals("uploads/pdf/nuevo.pdf", editada.getFragmento().getUrl());
        assertEquals(1, editada.getGeneros().size());
    }

    @Test
    public void otroEscritorNoPuedeEditarNiRetirar() throws Exception {
        ObraLiteraria obra = gestionObraServicio.crearObra(AUTOR, datosValidos());
        DatosObra cambios = datosValidos();
        cambios.setTitulo("Robada");

        Exception e = assertThrows(Exception.class, () -> gestionObraServicio.actualizarObra(OTRO, obra.getId(), cambios));
        assertEquals("No tienes permiso para modificar esta obra", e.getMessage());
        assertThrows(Exception.class, () -> gestionObraServicio.retirarObra(OTRO, obra.getId()));
        assertThrows(Exception.class, () -> gestionObraServicio.obtenerObraDeAutor(OTRO, obra.getId()));
        assertFalse(gestionObraServicio.esAutorDeObra(OTRO, obra.getId()));
        assertTrue(gestionObraServicio.esAutorDeObra(AUTOR, obra.getId()));

        ObraLiteraria guardada = obraRepo.findById(obra.getId()).orElseThrow();
        assertEquals("El faro de Salento", guardada.getTitulo());
        assertEquals(Estado.PUBLICADO, guardada.getEstado());

        assertThrows(Exception.class, () -> gestionObraServicio.retirarObra(AUTOR, 999999L));
    }

    @Test
    public void retirarYVolverAPublicar() throws Exception {
        ObraLiteraria obra = gestionObraServicio.crearObra(AUTOR, datosValidos());
        Long idPublicacion = obra.getPublicacion().getId();

        gestionObraServicio.retirarObra(AUTOR, obra.getId());
        ObraLiteraria retirada = obraRepo.findById(obra.getId()).orElseThrow();
        assertEquals(Estado.INACTIVO, retirada.getEstado());
        assertEquals(Estado.INACTIVO, retirada.getPublicacion().getEstado());
        assertFalse(ids(publicacionServicio.listarPublicacionesPublicadas()).contains(idPublicacion));
        assertFalse(ids(publicacionServicio.buscarPublicacionPorFrase("faro de salento")).contains(idPublicacion));
        assertFalse(ids(escritorServicio.obtenerPublicacionesDeEscritor(AUTOR)).contains(idPublicacion));
        assertThrows(Exception.class, () -> gestionObraServicio.retirarObra(AUTOR, obra.getId()));

        gestionObraServicio.volverAPublicarObra(AUTOR, obra.getId());
        assertTrue(ids(publicacionServicio.listarPublicacionesPublicadas()).contains(idPublicacion));
    }
}
