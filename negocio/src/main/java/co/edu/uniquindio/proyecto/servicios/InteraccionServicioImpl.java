package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.GeneroRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.repositorios.PublicacionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class InteraccionServicioImpl implements InteraccionServicio {

    private final ComentarioRepo comentarioRepo;
    private final PublicacionRepo publicacionRepo;
    private final LectorRepo lectorRepo;
    private final EscritorRepo escritorRepo;
    private final GeneroRepo generoRepo;
    private final FiltroPalabrasServicio filtroPalabras;

    public InteraccionServicioImpl(ComentarioRepo comentarioRepo, PublicacionRepo publicacionRepo, LectorRepo lectorRepo,
                                   EscritorRepo escritorRepo, GeneroRepo generoRepo, FiltroPalabrasServicio filtroPalabras) {
        this.comentarioRepo = comentarioRepo;
        this.publicacionRepo = publicacionRepo;
        this.lectorRepo = lectorRepo;
        this.escritorRepo = escritorRepo;
        this.generoRepo = generoRepo;
        this.filtroPalabras = filtroPalabras;
    }

    // ---- Comentarios ----

    @Override
    public Comentario comentar(Long idPublicacion, Usuario autor, String texto) throws Exception {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new Exception("El comentario no puede estar vacío");
        }
        if (limpio.length() > COMENTARIO_MAX) {
            throw new Exception("El comentario no puede tener más de " + COMENTARIO_MAX + " caracteres");
        }
        Publicacion publicacion = publicacionPublicada(idPublicacion);

        Comentario comentario = new Comentario();
        if (autor instanceof Lector && autor.getId() != null) {
            comentario.setLector(lectorRepo.findById(autor.getId())
                    .orElseThrow(() -> new Exception("Debes iniciar sesión para comentar")));
        } else if (autor instanceof Escritor && autor.getId() != null) {
            comentario.setEscritor(escritorRepo.findById(autor.getId())
                    .orElseThrow(() -> new Exception("Debes iniciar sesión para comentar")));
        } else {
            throw new Exception("Debes iniciar sesión como lector o escritor para comentar");
        }
        comentario.setContenido(filtroPalabras.censurar(limpio));
        comentario.setFechaPublicacion(new Date());
        comentario.setEstado(Estado.APROBADO);
        comentario.setPublicacion(publicacion);
        return comentarioRepo.save(comentario);
    }

    @Override
    public void eliminarComentario(Long idComentario, Usuario solicitante) throws Exception {
        Comentario comentario = idComentario == null ? null : comentarioRepo.findById(idComentario).orElse(null);
        if (comentario == null) {
            throw new Exception("El comentario no existe");
        }
        if (!esAutorDeComentario(comentario, solicitante)) {
            throw new Exception("Solo puedes eliminar tus propios comentarios");
        }
        comentarioRepo.delete(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comentario> listarComentarios(Long idPublicacion) {
        return idPublicacion == null ? new ArrayList<>() : comentarioRepo.listarComentariosVisibles(idPublicacion);
    }

    @Override
    public boolean esAutorDeComentario(Comentario comentario, Usuario usuario) {
        if (comentario == null || usuario == null || usuario.getId() == null) {
            return false;
        }
        if (comentario.getLector() != null) {
            return usuario instanceof Lector && usuario.getId().equals(comentario.getLector().getId());
        }
        return comentario.getEscritor() != null && usuario instanceof Escritor
                && usuario.getId().equals(comentario.getEscritor().getId());
    }

    // ---- Recomendaciones y vistas ----

    @Override
    public boolean alternarRecomendacion(Long idLector, Long idPublicacion) throws Exception {
        Lector lector = lector(idLector);
        Publicacion publicacion = publicacionPublicada(idPublicacion);
        boolean quitada = lector.getPublicacionesRecomendadas().removeIf(p -> p.getId().equals(publicacion.getId()));
        if (!quitada) {
            lector.getPublicacionesRecomendadas().add(publicacion);
        }
        lectorRepo.save(lector);
        return !quitada;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean haRecomendado(Long idLector, Long idPublicacion) {
        return idLector != null && idPublicacion != null && publicacionRepo.haRecomendado(idLector, idPublicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarRecomendaciones(Long idPublicacion) {
        return idPublicacion == null ? 0 : publicacionRepo.contarRecomendaciones(idPublicacion);
    }

    @Override
    public void registrarVista(Long idLector, Long idPublicacion) {
        if (idLector == null || idPublicacion == null) {
            return;
        }
        Lector lector = lectorRepo.findById(idLector).orElse(null);
        Publicacion publicacion = publicacionRepo.findById(idPublicacion).orElse(null);
        if (lector == null || publicacion == null || publicacion.getEstado() != Estado.PUBLICADO) {
            return;
        }
        boolean yaVista = lector.getPublicacionesVistas().stream().anyMatch(p -> p.getId().equals(idPublicacion));
        if (!yaVista) {
            lector.getPublicacionesVistas().add(publicacion);
            lectorRepo.save(lector);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVistas(Long idPublicacion) {
        return idPublicacion == null ? 0 : publicacionRepo.contarVistas(idPublicacion);
    }

    // ---- Escritores favoritos ----

    @Override
    public boolean alternarFavorito(Long idLector, Long idEscritor) throws Exception {
        Lector lector = lector(idLector);
        Escritor escritor = idEscritor == null ? null : escritorRepo.findById(idEscritor).orElse(null);
        if (escritor == null) {
            throw new Exception("El escritor no existe");
        }
        boolean quitado = lector.getEscritoresFavoritos().removeIf(e -> e.getId().equals(escritor.getId()));
        if (!quitado) {
            lector.getEscritoresFavoritos().add(escritor);
        }
        lectorRepo.save(lector);
        return !quitado;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esFavorito(Long idLector, Long idEscritor) {
        return idLector != null && idEscritor != null && lectorRepo.esFavorito(idLector, idEscritor);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarSeguidores(Long idEscritor) {
        return idEscritor == null ? 0 : lectorRepo.contarSeguidores(idEscritor);
    }

    // ---- Géneros preferidos y recomendaciones personalizadas ----

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerGenerosPreferidos(Long idLector) {
        return idLector == null ? new ArrayList<>() : lectorRepo.idsGenerosPreferidos(idLector);
    }

    @Override
    public void actualizarGenerosPreferidos(Long idLector, List<Long> idsGeneros) throws Exception {
        Lector lector = lector(idLector);
        Set<Long> ids = idsGeneros == null ? new LinkedHashSet<>() : new LinkedHashSet<>(idsGeneros);
        List<Genero> generos = generoRepo.findAllById(ids);
        if (generos.size() != ids.size()) {
            throw new Exception("Alguno de los géneros elegidos no existe");
        }
        lector.getGenerosPreferidos().clear();
        lector.getGenerosPreferidos().addAll(generos);
        lectorRepo.save(lector);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Publicacion> recomendadasPara(Long idLector, int maximo) {
        if (idLector == null || maximo <= 0) {
            return new ArrayList<>();
        }
        List<Long> generos = lectorRepo.idsGenerosPreferidos(idLector);
        List<Long> escritores = lectorRepo.idsEscritoresFavoritos(idLector);
        if (generos.isEmpty() && escritores.isEmpty()) {
            return new ArrayList<>();
        }
        // JPQL no admite listas vacías en IN: se usa un id que no existe
        List<Publicacion> candidatas = publicacionRepo.buscarPublicadasPorGenerosOEscritores(
                generos.isEmpty() ? List.of(-1L) : generos,
                escritores.isEmpty() ? List.of(-1L) : escritores);
        Set<Long> vistas = new HashSet<>(lectorRepo.idsPublicacionesVistas(idLector));
        Map<Long, Long> popularidad = popularidad();
        return candidatas.stream()
                .sorted(Comparator.<Publicacion, Boolean>comparing(p -> vistas.contains(p.getId()))
                        .thenComparing(p -> popularidad.getOrDefault(p.getId(), 0L), Comparator.reverseOrder())
                        .thenComparing(Publicacion::getId, Comparator.reverseOrder()))
                .limit(maximo)
                .collect(Collectors.toList());
    }

    // ---- Búsqueda ----

    @Override
    @Transactional(readOnly = true)
    public List<Publicacion> buscarPublicaciones(String frase, Long idGenero, Orden orden) {
        String patron = patron(frase);
        List<Publicacion> resultado = idGenero == null
                ? publicacionRepo.buscarPublicadas(patron)
                : publicacionRepo.buscarPublicadasPorGenero(patron, idGenero);
        resultado = new ArrayList<>(resultado);
        if (orden == Orden.POPULARES) {
            Map<Long, Long> popularidad = popularidad();
            resultado.sort(Comparator.<Publicacion, Long>comparing(p -> popularidad.getOrDefault(p.getId(), 0L))
                    .reversed()
                    .thenComparing(Publicacion::getId, Comparator.reverseOrder()));
        } else {
            resultado.sort(Comparator.comparing(Publicacion::getFechaPublicacion, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Publicacion::getId, Comparator.reverseOrder()));
        }
        return resultado;
    }

    // ---------------------------------------------------------------------------------------------

    private Lector lector(Long idLector) throws Exception {
        if (idLector == null) {
            throw new Exception("Debes iniciar sesión como lector");
        }
        return lectorRepo.findById(idLector).orElseThrow(() -> new Exception("Debes iniciar sesión como lector"));
    }

    private Publicacion publicacionPublicada(Long idPublicacion) throws Exception {
        Publicacion publicacion = idPublicacion == null ? null : publicacionRepo.findById(idPublicacion).orElse(null);
        if (publicacion == null || publicacion.getEstado() != Estado.PUBLICADO) {
            throw new Exception("La publicación no existe o ya no está disponible");
        }
        return publicacion;
    }

    /** Popularidad = vistas + recomendaciones de cada publicación. */
    private Map<Long, Long> popularidad() {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] fila : publicacionRepo.contarVistasPorPublicacion()) {
            mapa.merge((Long) fila[0], (Long) fila[1], Long::sum);
        }
        for (Object[] fila : publicacionRepo.contarRecomendacionesPorPublicacion()) {
            mapa.merge((Long) fila[0], (Long) fila[1], Long::sum);
        }
        return mapa;
    }

    /** Patrón LIKE en minúsculas; escapa los comodines que escriba el usuario (! es el carácter de escape). */
    private static String patron(String frase) {
        if (frase == null || frase.isBlank()) {
            return "%";
        }
        String escapada = frase.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escapada + "%";
    }
}
