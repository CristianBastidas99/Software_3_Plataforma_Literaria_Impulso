package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.servicios.FiltroPalabrasServicio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** No necesita Spring ni base de datos: el filtro solo lee malaspalabras.json del classpath. */
public class FiltroPalabrasServicioTest {

    private final FiltroPalabrasServicio filtro = new FiltroPalabrasServicio();

    @Test
    public void cargaLaLista() {
        assertTrue(filtro.getTotalPalabras() > 100);
    }

    @Test
    public void censuraSinDistinguirMayusculasNiTildes() {
        assertEquals("Eres un ********* y un *********.", filtro.censurar("Eres un BOBALICÓN y un bobalicon."));
        assertEquals("¡*******!", filtro.censurar("¡Bellaco!"));
        assertEquals("Qué *********", filtro.censurar("Qué alfeñique"));
        assertEquals("Qué *********", filtro.censurar("Qué ALFEÑIQUE"));
        assertTrue(filtro.contieneOfensivas("menudo BeSuGo"));
    }

    @Test
    public void soloPalabrasCompletas() {
        String texto = "Los bobaliconesque y el besugoso no están en la lista.";
        assertEquals(texto, filtro.censurar(texto));
        assertFalse(filtro.contieneOfensivas(texto));
    }

    @Test
    public void textoLimpioNoCambia() {
        String texto = "Me encantó la historia, sobre todo el final.";
        assertEquals(texto, filtro.censurar(texto));
        assertNull(filtro.censurar(null));
        assertEquals("", filtro.censurar(""));
    }
}
