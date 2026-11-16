package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.Estadisticas;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.entidades.ObraLiteraria;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.GeneroRepo;
import co.edu.uniquindio.proyecto.repositorios.LectorRepo;
import co.edu.uniquindio.proyecto.repositorios.ObraLiterariaRepo;
import co.edu.uniquindio.proyecto.repositorios.PublicacionRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class AdministracionServicioImpl implements AdministracionServicio {

    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;
    private final PublicacionRepo publicacionRepo;
    private final ObraLiterariaRepo obraRepo;
    private final ComentarioRepo comentarioRepo;
    private final GeneroRepo generoRepo;

    public AdministracionServicioImpl(EscritorRepo escritorRepo, LectorRepo lectorRepo, PublicacionRepo publicacionRepo,
                                      ObraLiterariaRepo obraRepo, ComentarioRepo comentarioRepo, GeneroRepo generoRepo) {
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
        this.publicacionRepo = publicacionRepo;
        this.obraRepo = obraRepo;
        this.comentarioRepo = comentarioRepo;
        this.generoRepo = generoRepo;
    }

    // ---- Usuarios ----

    @Override
    @Transactional(readOnly = true)
    public List<Escritor> listarEscritores() {
        return escritorRepo.findAll(Sort.by("nombre"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lector> listarLectores() {
        return lectorRepo.findAll(Sort.by("nombre"));
    }

    @Override
    public void cambiarEstadoEscritor(Long idEscritor, boolean activo) throws Exception {
        Escritor escritor = idEscritor == null ? null : escritorRepo.findById(idEscritor).orElse(null);
        if (escritor == null) {
            throw new Exception("El escritor no existe");
        }
        escritor.setEstado(activo ? Estado.ACTIVO : Estado.INACTIVO);
        escritorRepo.save(escritor);
    }

    @Override
    public void cambiarEstadoLector(Long idLector, boolean activo) throws Exception {
        Lector lector = idLector == null ? null : lectorRepo.findById(idLector).orElse(null);
        if (lector == null) {
            throw new Exception("El lector no existe");
        }
        lector.setEstado(activo ? Estado.ACTIVO : Estado.INACTIVO);
        lectorRepo.save(lector);
    }

    // ---- Publicaciones ----

    @Override
    @Transactional(readOnly = true)
    public List<Publicacion> listarPublicaciones() {
        return publicacionRepo.listarTodasRecientes();
    }

    @Override
    public void ocultarPublicacion(Long idPublicacion) throws Exception {
        Publicacion publicacion = publicacion(idPublicacion);
        if (publicacion.getEstado() == Estado.RECHAZADO) {
            throw new Exception("La publicación ya está oculta");
        }
        cambiarEstado(publicacion, Estado.RECHAZADO);
    }

    @Override
    public void mostrarPublicacion(Long idPublicacion) throws Exception {
        Publicacion publicacion = publicacion(idPublicacion);
        if (publicacion.getEstado() != Estado.RECHAZADO) {
            throw new Exception("Solo se pueden volver a mostrar publicaciones ocultadas por el administrador");
        }
        cambiarEstado(publicacion, Estado.PUBLICADO);
    }

    @Override
    public List<String> eliminarPublicacion(Long idPublicacion) throws Exception {
        Publicacion publicacion = publicacion(idPublicacion);
        List<String> archivos = new ArrayList<>();
        archivos.add(publicacion.getUrlImagen());
        ObraLiteraria obra = publicacion.getObraLiteraria();
        Long idObra = obra == null ? null : obra.getId();
        if (obra != null && obra.getFragmento() != null) {
            archivos.add(obra.getFragmento().getUrl());
        }

        // Primero lo que referencia a la publicación desde otras tablas
        comentarioRepo.eliminarComentariosDePublicacion(idPublicacion);
        publicacionRepo.eliminarVistasDePublicacion(idPublicacion);
        publicacionRepo.eliminarRecomendacionesDePublicacion(idPublicacion);

        // Las consultas anteriores vacían el contexto de persistencia: se recargan las entidades
        if (idObra != null) {
            // La obra borra en cascada su fragmento y su publicación, y sus filas de géneros y autores
            obraRepo.findById(idObra).ifPresent(obraRepo::delete);
        } else {
            publicacionRepo.findById(idPublicacion).ifPresent(publicacionRepo::delete);
        }
        return archivos.stream().filter(a -> a != null && !a.isBlank()).collect(Collectors.toList());
    }

    // ---- Comentarios ----

    @Override
    @Transactional(readOnly = true)
    public List<Comentario> listarComentarios() {
        return comentarioRepo.listarComentariosMasRecientes();
    }

    @Override
    public void ocultarComentario(Long idComentario) throws Exception {
        Comentario comentario = comentario(idComentario);
        comentario.setEstado(Estado.RECHAZADO);
        comentarioRepo.save(comentario);
    }

    @Override
    public void mostrarComentario(Long idComentario) throws Exception {
        Comentario comentario = comentario(idComentario);
        comentario.setEstado(Estado.APROBADO);
        comentarioRepo.save(comentario);
    }

    @Override
    public void eliminarComentario(Long idComentario) throws Exception {
        comentarioRepo.delete(comentario(idComentario));
    }

    // ---- Géneros ----

    @Override
    @Transactional(readOnly = true)
    public List<Genero> listarGeneros() {
        List<Genero> generos = new ArrayList<>(generoRepo.findAll());
        generos.sort(Comparator.comparing(Genero::getNombre, String.CASE_INSENSITIVE_ORDER));
        return generos;
    }

    @Override
    public Genero crearGenero(String nombre) throws Exception {
        String limpio = nombreValido(nombre, null);
        Genero genero = new Genero();
        genero.setNombre(limpio);
        return generoRepo.save(genero);
    }

    @Override
    public Genero renombrarGenero(Long idGenero, String nombre) throws Exception {
        Genero genero = genero(idGenero);
        genero.setNombre(nombreValido(nombre, genero.getId()));
        return generoRepo.save(genero);
    }

    @Override
    public void eliminarGenero(Long idGenero) throws Exception {
        Genero genero = genero(idGenero);
        long obras = generoRepo.contarObrasDeGenero(genero.getId());
        if (obras > 0) {
            throw new Exception("No se puede eliminar «" + genero.getNombre() + "»: tiene " + obras
                    + (obras == 1 ? " obra asociada" : " obras asociadas"));
        }
        generoRepo.eliminarPreferenciasDeGenero(genero.getId());
        generoRepo.findById(genero.getId()).ifPresent(generoRepo::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarObrasDeGenero(Long idGenero) {
        return idGenero == null ? 0 : generoRepo.contarObrasDeGenero(idGenero);
    }

    // ---- Estadísticas ----

    @Override
    @Transactional(readOnly = true)
    public Estadisticas obtenerEstadisticas(int maxMasVistas) {
        Estadisticas e = new Estadisticas();
        e.setEscritoresActivos(escritorRepo.countByEstado(Estado.ACTIVO));
        e.setEscritoresBloqueados(escritorRepo.countByEstado(Estado.INACTIVO));
        e.setLectoresActivos(lectorRepo.countByEstado(Estado.ACTIVO));
        e.setLectoresBloqueados(lectorRepo.countByEstado(Estado.INACTIVO));
        e.setObras(obraRepo.count());
        e.setPublicacionesPublicadas(publicacionRepo.countByEstado(Estado.PUBLICADO));
        e.setPublicacionesOcultas(publicacionRepo.countByEstado(Estado.RECHAZADO));
        e.setPublicacionesRetiradas(publicacionRepo.countByEstado(Estado.INACTIVO));
        e.setComentarios(comentarioRepo.count());
        e.setComentariosOcultos(comentarioRepo.countByEstado(Estado.RECHAZADO));
        if (maxMasVistas > 0) {
            e.setMasVistas(publicacionRepo.listarMasVistas(PageRequest.of(0, maxMasVistas)).stream()
                    .map(f -> new Estadisticas.PublicacionVistas((Long) f[0], (String) f[1], (Long) f[2]))
                    .collect(Collectors.toList()));
        }
        return e;
    }

    // ---------------------------------------------------------------------------------------------

    private Publicacion publicacion(Long id) throws Exception {
        Optional<Publicacion> p = id == null ? Optional.empty() : publicacionRepo.findById(id);
        return p.orElseThrow(() -> new Exception("La publicación no existe"));
    }

    private Comentario comentario(Long id) throws Exception {
        Optional<Comentario> c = id == null ? Optional.empty() : comentarioRepo.findById(id);
        return c.orElseThrow(() -> new Exception("El comentario no existe"));
    }

    private Genero genero(Long id) throws Exception {
        Optional<Genero> g = id == null ? Optional.empty() : generoRepo.findById(id);
        return g.orElseThrow(() -> new Exception("El género no existe"));
    }

    /** La publicación y su obra cambian de estado a la vez. */
    private void cambiarEstado(Publicacion publicacion, Estado estado) {
        publicacion.setEstado(estado);
        publicacionRepo.save(publicacion);
        ObraLiteraria obra = publicacion.getObraLiteraria();
        if (obra != null) {
            obra.setEstado(estado);
            obraRepo.save(obra);
        }
    }

    private String nombreValido(String nombre, Long idActual) throws Exception {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new Exception("El nombre del género no puede estar vacío");
        }
        if (limpio.length() > GENERO_MAX) {
            throw new Exception("El nombre del género no puede tener más de " + GENERO_MAX + " caracteres");
        }
        Optional<Genero> existente = generoRepo.findByNombreIgnoreCase(limpio);
        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new Exception("Ya existe un género llamado «" + existente.get().getNombre() + "»");
        }
        return limpio;
    }
}
