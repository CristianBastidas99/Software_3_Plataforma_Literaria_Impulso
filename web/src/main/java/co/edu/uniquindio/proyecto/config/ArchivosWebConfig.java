package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.archivos.ArchivoServicio;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve la carpeta de archivos subidos ({@code upload.dir}) en {@code /uploads/**}.
 * Los nombres son UUID y nunca cambian, así que se pueden cachear en el navegador.
 */
@Configuration
public class ArchivosWebConfig implements WebMvcConfigurer {

    private static final int UN_MES_EN_SEGUNDOS = 60 * 60 * 24 * 30;

    private final ArchivoServicio archivoServicio;

    public ArchivosWebConfig(ArchivoServicio archivoServicio) {
        this.archivoServicio = archivoServicio;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = archivoServicio.getDirectorioBase().toUri().toString();
        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }
        registry.addResourceHandler("/" + ArchivoServicio.PREFIJO + "**")
                .addResourceLocations(ubicacion)
                .setCachePeriod(UN_MES_EN_SEGUNDOS);
    }
}
