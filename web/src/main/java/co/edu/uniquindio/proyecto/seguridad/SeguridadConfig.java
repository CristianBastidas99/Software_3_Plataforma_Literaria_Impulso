package co.edu.uniquindio.proyecto.seguridad;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SeguridadConfig {

    @Bean
    public FilterRegistrationBean<ControlAccesoFilter> controlAccesoFilter() {
        FilterRegistrationBean<ControlAccesoFilter> registro = new FilterRegistrationBean<>(new ControlAccesoFilter());
        registro.addUrlPatterns("/*");
        registro.setName("controlAcceso");
        // Después de los filtros de Spring (codificación, contexto de la petición)
        registro.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return registro;
    }
}
