package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Carga los datos de demostración (géneros, usuarios de los tres roles, obras con portada y
 * fragmento PDF, comentarios e interacciones) desde {@code demo/datos-demo.json}.
 *
 * <p>Solo se ejecuta con el perfil {@code demo} y únicamente si la base de datos no tiene
 * usuarios ni publicaciones, así que arrancar varias veces con el perfil no duplica nada.</p>
 *
 * <p>Las imágenes y PDFs referenciados se generan con {@code scripts/generar_recursos_demo.py}
 * y están en {@code src/main/webapp/resources/img} y {@code resources/pdf}.</p>
 */
@Component
@Profile("demo")
public class DatosDemo implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosDemo.class);
    private static final String ARCHIVO = "demo/datos-demo.json";

    private final GeneroRepo generoRepo;
    private final AdministradorRepo administradorRepo;
    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;
    private final ObraLiterariaRepo obraLiterariaRepo;
    private final FragmentoRepo fragmentoRepo;
    private final PublicacionRepo publicacionRepo;
    private final ComentarioRepo comentarioRepo;

    public DatosDemo(GeneroRepo generoRepo, AdministradorRepo administradorRepo, EscritorRepo escritorRepo,
                     LectorRepo lectorRepo, ObraLiterariaRepo obraLiterariaRepo, FragmentoRepo fragmentoRepo,
                     PublicacionRepo publicacionRepo, ComentarioRepo comentarioRepo) {
        this.generoRepo = generoRepo;
        this.administradorRepo = administradorRepo;
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
        this.obraLiterariaRepo = obraLiterariaRepo;
        this.fragmentoRepo = fragmentoRepo;
        this.publicacionRepo = publicacionRepo;
        this.comentarioRepo = comentarioRepo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (administradorRepo.count() > 0 || escritorRepo.count() > 0 || lectorRepo.count() > 0
                || publicacionRepo.count() > 0) {
            LOG.info("Perfil demo: la base de datos ya tiene datos, no se cargan los datos de demostración.");
            return;
        }

        JsonNode datos;
        try (InputStream entrada = new ClassPathResource(ARCHIVO).getInputStream()) {
            datos = new ObjectMapper().readTree(entrada);
        }

        Map<String, Genero> generos = cargarGeneros(datos.get("generos"));
        cargarAdministrador(datos.get("administrador"));
        Map<String, Escritor> escritores = cargarEscritores(datos.get("escritores"));
        Map<String, Publicacion> publicaciones = cargarObras(datos.get("obras"), escritores, generos);
        Map<String, Lector> lectores = cargarLectores(datos.get("lectores"), escritores, generos, publicaciones);
        int comentarios = cargarComentarios(datos.get("comentarios"), escritores, lectores, publicaciones);

        LOG.info("Perfil demo: se cargaron {} géneros, 1 administrador, {} escritores, {} lectores, {} obras y {} comentarios.",
                generos.size(), escritores.size(), lectores.size(), publicaciones.size(), comentarios);
    }

    private Map<String, Genero> cargarGeneros(JsonNode nodos) {
        Map<String, Genero> generos = new LinkedHashMap<>();
        for (JsonNode nodo : nodos) {
            Genero genero = new Genero();
            genero.setNombre(nodo.asText());
            generos.put(genero.getNombre(), generoRepo.save(genero));
        }
        return generos;
    }

    private void cargarAdministrador(JsonNode nodo) {
        Administrador administrador = new Administrador();
        llenarUsuario(administrador, nodo);
        administradorRepo.save(administrador);
    }

    private Map<String, Escritor> cargarEscritores(JsonNode nodos) {
        Map<String, Escritor> escritores = new LinkedHashMap<>();
        for (JsonNode nodo : nodos) {
            Escritor escritor = new Escritor();
            llenarUsuario(escritor, nodo);
            escritor.setBiografia(nodo.get("biografia").asText());
            escritores.put(nodo.get("clave").asText(), escritorRepo.save(escritor));
        }
        return escritores;
    }

    /** Crea cada obra con su fragmento PDF y su publicación. Devuelve las publicaciones por clave de obra. */
    private Map<String, Publicacion> cargarObras(JsonNode nodos, Map<String, Escritor> escritores,
                                                 Map<String, Genero> generos) {
        Map<String, Publicacion> publicaciones = new LinkedHashMap<>();
        for (JsonNode nodo : nodos) {
            Date fecha = aFecha(LocalDate.parse(nodo.get("fecha").asText()));

            ObraLiteraria obra = new ObraLiteraria();
            obra.setTitulo(nodo.get("titulo").asText());
            obra.setSinopsis(nodo.get("sinopsis").asText());
            obra.setIsbn(nodo.get("isbn").asText());
            obra.setEditorial(nodo.get("editorial").asText());
            obra.setFechaPublicacion(fecha);
            obra.setEstado(Estado.PUBLICADO);
            obra.setEscritores(buscar(nodo.get("autores"), escritores));
            obra.setGeneros(buscar(nodo.get("generos"), generos));
            obra = obraLiterariaRepo.save(obra);

            Fragmento fragmento = new Fragmento();
            fragmento.setUrl(nodo.get("fragmento").asText());
            fragmento.setEstado(Estado.APROBADO);
            fragmento.setObraLiteraria(obra);
            fragmentoRepo.save(fragmento);

            Publicacion publicacion = new Publicacion();
            publicacion.setTitulo(obra.getTitulo());
            publicacion.setContenido(nodo.get("contenido").asText());
            publicacion.setUrlImagen(nodo.get("portada").asText());
            publicacion.setFechaPublicacion(fecha);
            publicacion.setEstado(Estado.PUBLICADO);
            publicacion.setObraLiteraria(obra);
            publicaciones.put(nodo.get("clave").asText(), publicacionRepo.save(publicacion));
        }
        return publicaciones;
    }

    private Map<String, Lector> cargarLectores(JsonNode nodos, Map<String, Escritor> escritores,
                                               Map<String, Genero> generos, Map<String, Publicacion> publicaciones) {
        Map<String, Lector> lectores = new LinkedHashMap<>();
        for (JsonNode nodo : nodos) {
            Lector lector = new Lector();
            llenarUsuario(lector, nodo);
            lector.setGenerosPreferidos(buscar(nodo.get("generosPreferidos"), generos));
            lector.setEscritoresFavoritos(buscar(nodo.get("escritoresFavoritos"), escritores));
            lector.setPublicacionesRecomendadas(buscar(nodo.get("publicacionesRecomendadas"), publicaciones));
            lector.setPublicacionesVistas(buscar(nodo.get("publicacionesVistas"), publicaciones));
            lectores.put(nodo.get("clave").asText(), lectorRepo.save(lector));
        }
        return lectores;
    }

    private int cargarComentarios(JsonNode nodos, Map<String, Escritor> escritores, Map<String, Lector> lectores,
                                  Map<String, Publicacion> publicaciones) {
        int total = 0;
        for (JsonNode nodo : nodos) {
            Comentario comentario = new Comentario();
            comentario.setContenido(nodo.get("contenido").asText());
            comentario.setFechaPublicacion(Date.from(LocalDateTime.parse(nodo.get("fecha").asText())
                    .atZone(ZoneId.systemDefault()).toInstant()));
            comentario.setEstado(Estado.APROBADO);
            comentario.setPublicacion(obtener(publicaciones, nodo.get("obra").asText()));
            if (nodo.has("lector")) {
                comentario.setLector(obtener(lectores, nodo.get("lector").asText()));
            } else {
                comentario.setEscritor(obtener(escritores, nodo.get("escritor").asText()));
            }
            comentarioRepo.save(comentario);
            total++;
        }
        return total;
    }

    private static void llenarUsuario(Usuario usuario, JsonNode nodo) {
        usuario.setNombre(nodo.get("nombre").asText());
        usuario.setEmail(nodo.get("email").asText());
        usuario.setPassword(nodo.get("password").asText());
        usuario.setUrlImagen(nodo.get("imagen").asText());
        usuario.setEstado(Estado.ACTIVO);
    }

    private static <T> List<T> buscar(JsonNode claves, Map<String, T> mapa) {
        List<T> resultado = new ArrayList<>();
        for (JsonNode clave : claves) {
            resultado.add(obtener(mapa, clave.asText()));
        }
        return resultado;
    }

    private static <T> T obtener(Map<String, T> mapa, String clave) {
        T valor = mapa.get(clave);
        if (valor == null) {
            throw new IllegalStateException("datos-demo.json hace referencia a una clave inexistente: " + clave);
        }
        return valor;
    }

    private static Date aFecha(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
