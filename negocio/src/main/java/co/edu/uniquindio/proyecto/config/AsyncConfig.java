package co.edu.uniquindio.proyecto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Activa {@code @Async} para enviar los correos en segundo plano (usa el ejecutor que Spring Boot
 * configura por defecto). Así un SMTP lento o caído no bloquea la interfaz.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
