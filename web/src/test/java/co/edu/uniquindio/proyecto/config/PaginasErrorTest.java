package co.edu.uniquindio.proyecto.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.ErrorPage;

import javax.faces.application.ViewExpiredException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginasErrorTest {

    private List<ErrorPage> registrar() {
        List<ErrorPage> paginas = new ArrayList<>();
        new PaginasError().registerErrorPages(nuevas -> paginas.addAll(Arrays.asList(nuevas)));
        return paginas;
    }

    @Test
    void registraLaPagina404Propia() {
        assertTrue(registrar().stream().anyMatch(p -> p.getStatusCode() == 404
                && "/errores/404.xhtml".equals(p.getPath())));
    }

    @Test
    void registraLaPagina500ParaErroresYVistasCaducadas() {
        List<ErrorPage> paginas = registrar();
        assertTrue(paginas.stream().anyMatch(p -> p.getStatusCode() == 500
                && "/errores/500.xhtml".equals(p.getPath())));
        assertTrue(paginas.stream().anyMatch(p -> p.getException() == ViewExpiredException.class
                && "/errores/500.xhtml".equals(p.getPath())));
    }
}
