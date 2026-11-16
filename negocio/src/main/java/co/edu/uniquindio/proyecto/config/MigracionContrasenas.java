package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pone al día una base de datos creada antes de la Fase 9, al arrancar y antes que cualquier otro
 * proceso de inicio:
 * <ol>
 *     <li>Amplía la columna {@code password} a 100 caracteres ({@code ddl-auto=update} no cambia la
 *     longitud de una columna que ya existe, y un hash BCrypt no cabe en los 30 de antes).</li>
 *     <li>Cifra las contraseñas que sigan en claro. Cada usuario conserva su contraseña.</li>
 * </ol>
 * Si la base de datos ya está al día no hace nada.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MigracionContrasenas implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MigracionContrasenas.class);
    private static final int LONGITUD = 100;
    private static final String[] TABLAS = {"administrador", "escritor", "lector"};

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccion;
    private final AdministradorRepo administradorRepo;
    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;

    public MigracionContrasenas(JdbcTemplate jdbc, TransactionTemplate transaccion, AdministradorRepo administradorRepo,
                                EscritorRepo escritorRepo, LectorRepo lectorRepo) {
        this.jdbc = jdbc;
        this.transaccion = transaccion;
        this.administradorRepo = administradorRepo;
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String tabla : TABLAS) {
            ampliarColumna(tabla);
        }
        int cifradas = transaccion.execute(estado ->
                cifrarPendientes(administradorRepo) + cifrarPendientes(escritorRepo) + cifrarPendientes(lectorRepo));
        if (cifradas > 0) {
            LOG.info("Migración de seguridad: se cifraron {} contraseñas que estaban en texto plano.", cifradas);
        }
    }

    private void ampliarColumna(String tabla) {
        List<Long> longitud = jdbc.queryForList(
                "SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'password'",
                Long.class, tabla);
        if (!longitud.isEmpty() && longitud.get(0) != null && longitud.get(0) < LONGITUD) {
            // El nombre de la tabla sale de la constante TABLAS, nunca de datos externos
            jdbc.execute("ALTER TABLE " + tabla + " MODIFY password VARCHAR(" + LONGITUD + ") NOT NULL");
            LOG.info("Migración de seguridad: columna {}.password ampliada a {} caracteres.", tabla, LONGITUD);
        }
    }

    private <T extends Usuario> int cifrarPendientes(JpaRepository<T, Long> repo) {
        List<T> enClaro = repo.findAll().stream()
                .filter(u -> u.getPassword() != null && !Contrasenas.estaCifrada(u.getPassword()))
                .collect(Collectors.toList());
        enClaro.forEach(u -> u.setPassword(Contrasenas.cifrar(u.getPassword())));
        repo.saveAll(enClaro);
        return enClaro.size();
    }
}
