package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.DatosObra;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Estado;
import co.edu.uniquindio.proyecto.entidades.Fragmento;
import co.edu.uniquindio.proyecto.entidades.Genero;
import co.edu.uniquindio.proyecto.entidades.ObraLiteraria;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.repositorios.EscritorRepo;
import co.edu.uniquindio.proyecto.repositorios.GeneroRepo;
import co.edu.uniquindio.proyecto.repositorios.ObraLiterariaRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class GestionObraServicioImpl implements GestionObraServicio {

    private final ObraLiterariaRepo obraRepo;
    private final EscritorRepo escritorRepo;
    private final GeneroRepo generoRepo;
    private final Validator validator;

    public GestionObraServicioImpl(ObraLiterariaRepo obraRepo, EscritorRepo escritorRepo, GeneroRepo generoRepo,
                                   Validator validator) {
        this.obraRepo = obraRepo;
        this.escritorRepo = escritorRepo;
        this.generoRepo = generoRepo;
        this.validator = validator;
    }

    @Override
    public ObraLiteraria crearObra(Long idAutor, DatosObra datos) throws Exception {
        Escritor autor = autorActivo(idAutor);
        validar(datos);
        if (obraRepo.existsByIsbn(datos.getIsbn().trim())) {
            throw new Exception("Ya existe una obra con ese ISBN");
        }

        ObraLiteraria obra = new ObraLiteraria();
        aplicarDatos(obra, autor, datos);
        obra.setEstado(Estado.PUBLICADO);

        Fragmento fragmento = new Fragmento();
        fragmento.setUrl(datos.getUrlFragmento());
        fragmento.setEstado(Estado.APROBADO);
        fragmento.setObraLiteraria(obra);
        obra.setFragmento(fragmento);

        Publicacion publicacion = new Publicacion();
        publicacion.setFechaPublicacion(new Date());
        publicacion.setEstado(Estado.PUBLICADO);
        publicacion.setObraLiteraria(obra);
        aplicarDatos(publicacion, datos);
        obra.setPublicacion(publicacion);

        // La obra propaga el guardado (cascade ALL) a su fragmento y su publicación
        return obraRepo.save(obra);
    }

    @Override
    public ObraLiteraria actualizarObra(Long idAutor, Long idObra, DatosObra datos) throws Exception {
        Escritor autor = autorActivo(idAutor);
        ObraLiteraria obra = obraDeAutor(idAutor, idObra);
        validar(datos);
        if (obraRepo.existsByIsbnAndIdNot(datos.getIsbn().trim(), obra.getId())) {
            throw new Exception("Ya existe otra obra con ese ISBN");
        }

        aplicarDatos(obra, autor, datos);

        Fragmento fragmento = obra.getFragmento();
        if (fragmento == null) {
            fragmento = new Fragmento();
            fragmento.setEstado(Estado.APROBADO);
            fragmento.setObraLiteraria(obra);
            obra.setFragmento(fragmento);
        }
        fragmento.setUrl(datos.getUrlFragmento());

        Publicacion publicacion = obra.getPublicacion();
        if (publicacion == null) {
            publicacion = new Publicacion();
            publicacion.setFechaPublicacion(new Date());
            publicacion.setEstado(obra.getEstado());
            publicacion.setObraLiteraria(obra);
            obra.setPublicacion(publicacion);
        }
        aplicarDatos(publicacion, datos);

        return obraRepo.save(obra);
    }

    @Override
    @Transactional(readOnly = true)
    public ObraLiteraria obtenerObraDeAutor(Long idAutor, Long idObra) throws Exception {
        return obraDeAutor(idAutor, idObra);
    }

    @Override
    @Transactional(readOnly = true)
    public DatosObra obtenerDatosObra(Long idAutor, Long idObra) throws Exception {
        ObraLiteraria obra = obraDeAutor(idAutor, idObra);
        DatosObra datos = new DatosObra();
        datos.setTitulo(obra.getTitulo());
        datos.setSinopsis(obra.getSinopsis());
        datos.setIsbn(obra.getIsbn());
        datos.setEditorial(obra.getEditorial());
        datos.setFechaPublicacion(obra.getFechaPublicacion() == null ? null : new Date(obra.getFechaPublicacion().getTime()));
        datos.setGeneros(obra.getGeneros().stream().map(Genero::getId).collect(Collectors.toList()));
        datos.setCoautores(obra.getEscritores().stream().map(Escritor::getId)
                .filter(id -> !id.equals(idAutor)).collect(Collectors.toList()));
        if (obra.getPublicacion() != null) {
            datos.setDescripcion(obra.getPublicacion().getContenido());
            datos.setUrlPortada(obra.getPublicacion().getUrlImagen());
        }
        if (obra.getFragmento() != null) {
            datos.setUrlFragmento(obra.getFragmento().getUrl());
        }
        return datos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObraLiteraria> listarObrasDeAutor(Long idAutor) {
        return idAutor == null ? new ArrayList<>() : obraRepo.listarObrasDeEscritor(idAutor);
    }

    @Override
    public void retirarObra(Long idAutor, Long idObra) throws Exception {
        autorActivo(idAutor);
        ObraLiteraria obra = obraDeAutor(idAutor, idObra);
        if (obra.getEstado() != Estado.PUBLICADO) {
            throw new Exception("Solo se pueden retirar obras publicadas");
        }
        cambiarEstado(obra, Estado.INACTIVO);
    }

    @Override
    public void volverAPublicarObra(Long idAutor, Long idObra) throws Exception {
        autorActivo(idAutor);
        ObraLiteraria obra = obraDeAutor(idAutor, idObra);
        if (obra.getEstado() != Estado.INACTIVO) {
            throw new Exception("Solo se pueden volver a publicar obras retiradas");
        }
        if (obra.getFragmento() == null || obra.getFragmento().getUrl() == null || obra.getFragmento().getUrl().isBlank()) {
            throw new Exception("La obra necesita un fragmento en PDF para publicarse");
        }
        cambiarEstado(obra, Estado.PUBLICADO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esAutorDeObra(Long idAutor, Long idObra) {
        return idAutor != null && idObra != null && obraRepo.esAutorDeObra(idObra, idAutor);
    }

    // ---------------------------------------------------------------------------------------------

    private Escritor autorActivo(Long idAutor) throws Exception {
        if (idAutor == null) {
            throw new Exception("Debes iniciar sesión como escritor");
        }
        Escritor autor = escritorRepo.findById(idAutor)
                .orElseThrow(() -> new Exception("Debes iniciar sesión como escritor"));
        if (autor.getEstado() != Estado.ACTIVO) {
            throw new Exception("Tu cuenta de escritor no está activa");
        }
        return autor;
    }

    private ObraLiteraria obraDeAutor(Long idAutor, Long idObra) throws Exception {
        ObraLiteraria obra = idObra == null ? null : obraRepo.findById(idObra).orElse(null);
        if (obra == null) {
            throw new Exception("La obra no existe");
        }
        if (!obraRepo.esAutorDeObra(idObra, idAutor)) {
            throw new Exception("No tienes permiso para modificar esta obra");
        }
        return obra;
    }

    private void validar(DatosObra datos) throws Exception {
        if (datos == null) {
            throw new Exception("Faltan los datos de la obra");
        }
        Set<ConstraintViolation<DatosObra>> errores = validator.validate(datos);
        if (!errores.isEmpty()) {
            throw new Exception(errores.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(". ")));
        }
        if (datos.getUrlFragmento() == null || datos.getUrlFragmento().isBlank()) {
            throw new Exception("El fragmento en PDF es obligatorio");
        }
    }

    private void aplicarDatos(ObraLiteraria obra, Escritor autor, DatosObra datos) throws Exception {
        obra.setTitulo(datos.getTitulo().trim());
        obra.setSinopsis(datos.getSinopsis().trim());
        obra.setIsbn(datos.getIsbn().trim());
        obra.setEditorial(vacioANull(datos.getEditorial()));
        obra.setFechaPublicacion(datos.getFechaPublicacion());
        obra.setGeneros(generos(datos.getGeneros()));
        obra.setEscritores(firmantes(autor, datos.getCoautores()));
    }

    private void aplicarDatos(Publicacion publicacion, DatosObra datos) {
        publicacion.setTitulo(datos.getTitulo().trim());
        publicacion.setContenido(datos.getDescripcion().trim());
        publicacion.setUrlImagen(datos.getUrlPortada() == null ? "" : datos.getUrlPortada());
    }

    private List<Genero> generos(List<Long> ids) throws Exception {
        List<Genero> generos = generoRepo.findAllById(new LinkedHashSet<>(ids));
        if (generos.size() != new LinkedHashSet<>(ids).size()) {
            throw new Exception("Alguno de los géneros elegidos no existe");
        }
        generos.sort(Comparator.comparing(Genero::getNombre));
        return new ArrayList<>(generos);
    }

    /** El autor que guarda va primero; los coautores deben existir y estar activos. */
    private List<Escritor> firmantes(Escritor autor, List<Long> coautores) throws Exception {
        List<Escritor> firmantes = new ArrayList<>();
        firmantes.add(autor);
        if (coautores == null) {
            return firmantes;
        }
        Set<Long> ids = new LinkedHashSet<>(coautores);
        ids.remove(autor.getId());
        for (Long id : ids) {
            Escritor coautor = escritorRepo.findById(id)
                    .orElseThrow(() -> new Exception("Alguno de los coautores elegidos no existe"));
            if (coautor.getEstado() != Estado.ACTIVO) {
                throw new Exception("El coautor " + coautor.getNombre() + " no está activo");
            }
            firmantes.add(coautor);
        }
        return firmantes;
    }

    private void cambiarEstado(ObraLiteraria obra, Estado estado) {
        obra.setEstado(estado);
        if (obra.getPublicacion() != null) {
            obra.getPublicacion().setEstado(estado);
        }
        obraRepo.save(obra);
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
