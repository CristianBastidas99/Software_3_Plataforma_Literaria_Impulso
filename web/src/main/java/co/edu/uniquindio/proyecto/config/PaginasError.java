package co.edu.uniquindio.proyecto.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.faces.application.ViewExpiredException;

/**
 * Páginas de error propias, con la misma plantilla que el resto del sitio, en lugar de la página
 * genérica de Spring Boot o de Tomcat. Cubre tanto rutas inexistentes ("/algo") como vistas JSF
 * que no existen ("/algo.xhtml").
 */
@Component
public class PaginasError implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registro) {
        registro.addErrorPages(
                new ErrorPage(HttpStatus.NOT_FOUND, "/errores/404.xhtml"),
                new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errores/500.xhtml"),
                // Una vista que caducó (la página llevaba mucho tiempo abierta) también muestra la 500
                new ErrorPage(ViewExpiredException.class, "/errores/500.xhtml"));
    }
}
