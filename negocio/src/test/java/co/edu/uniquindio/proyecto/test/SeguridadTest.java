package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.config.MigracionContrasenas;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import co.edu.uniquindio.proyecto.servicios.LoginServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Contraseñas cifradas con BCrypt, login genérico y migración de contraseñas en claro. */
@SpringBootTest
@Transactional
@Sql("classpath:pli.sql")
public class SeguridadTest {

    @Autowired
    private LoginServicio loginServicio;
    @Autowired
    private EscritorServicio escritorServicio;
    @Autowired
    private AdministradorRepo administradorRepo;
    @Autowired
    private EscritorRepo escritorRepo;
    @Autowired
    private LectorRepo lectorRepo;
    @Autowired
    private MigracionContrasenas migracion;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManager em;

    @Test
    public void lasContrasenasSeGuardanCifradas() throws Exception {
        Escritor escritor = new Escritor();
        escritor.setNombre("Nueva Escritora");
        escritor.setEmail("nueva.seguridad@example.com");
        escritor.setPassword("clave-segura1");
        escritor.setUrlImagen("");
        escritor.setBiografia("");
        escritor.setEstado(Estado.ACTIVO);
        Escritor guardado = escritorServicio.registrarEscritor(escritor);
        em.flush();

        String hash = jdbc.queryForObject("SELECT password FROM escritor WHERE id = ?", String.class, guardado.getId());
        assertTrue(Contrasenas.estaCifrada(hash), "En la base de datos debe quedar un hash BCrypt");
        assertNotEquals("clave-segura1", hash);
        assertTrue(Contrasenas.coincide("clave-segura1", hash));

        // Guardar otra vez (por ejemplo al editar el perfil) no vuelve a cifrar el hash
        guardado.setNombre("Escritora Editada");
        escritorServicio.actualizarEscritor(guardado);
        em.flush();
        assertEquals(hash, jdbc.queryForObject("SELECT password FROM escritor WHERE id = ?", String.class, guardado.getId()));

        // Los datos de prueba también están cifrados
        assertTrue(Stream.of(administradorRepo.findAll(), escritorRepo.findAll(), lectorRepo.findAll())
                .flatMap(l -> l.stream().map(Usuario::getPassword))
                .allMatch(Contrasenas::estaCifrada));
    }

    @Test
    public void elLoginNoDelataQueCorreosExisten() throws Exception {
        Optional<Usuario> escritor = loginServicio.autenticar("writer1@example.com", "writerpass");
        assertTrue(escritor.isPresent());
        assertTrue(escritor.get() instanceof Escritor);
        assertTrue(loginServicio.autenticar("admin1@example.com", "password123").isPresent());
        assertTrue(loginServicio.autenticar(" reader1@example.com ", "readerpass").get() instanceof Lector);

        // Contraseña incorrecta y correo inexistente dan el mismo resultado
        assertTrue(loginServicio.autenticar("writer1@example.com", "otra").isEmpty());
        assertTrue(loginServicio.autenticar("nadie@example.com", "writerpass").isEmpty());
        assertTrue(loginServicio.autenticar(null, null).isEmpty());

        // La cuenta bloqueada solo se anuncia con la contraseña correcta
        Exception e = assertThrows(Exception.class, () -> loginServicio.autenticar("writer3@example.com", "passforwriter"));
        assertEquals(LoginServicio.CUENTA_BLOQUEADA, e.getMessage());
        assertTrue(loginServicio.autenticar("writer3@example.com", "incorrecta").isEmpty());
    }

    @Test
    public void laMigracionCifraLasContrasenasEnClaro() throws Exception {
        // Simula una base de datos anterior a la Fase 9 (escritura directa, sin pasar por JPA)
        jdbc.update("UPDATE lector SET password = 'antigua123' WHERE id = 2");
        em.clear();
        assertTrue(loginServicio.autenticar("reader2@example.com", "antigua123").isEmpty());

        migracion.run(null);
        // En la app la migración confirma su transacción; aquí comparte la de la prueba
        em.flush();
        em.clear();

        String hash = jdbc.queryForObject("SELECT password FROM lector WHERE id = 2", String.class);
        assertTrue(Contrasenas.estaCifrada(hash));
        assertTrue(loginServicio.autenticar("reader2@example.com", "antigua123").isPresent());
        // Las que ya estaban cifradas no cambian
        assertTrue(loginServicio.autenticar("reader1@example.com", "readerpass").isPresent());
    }
}
