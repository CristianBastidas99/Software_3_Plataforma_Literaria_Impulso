package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PublicacionServicioImpl implements PublicacionServicio{


    private final PublicacionRepo publicacionRepo;

    public PublicacionServicioImpl(PublicacionRepo publicacionRepo) {
        this.publicacionRepo = publicacionRepo;
    }

    @Override
    public Publicacion registrarPublicacion(Publicacion publicacion) throws Exception {
        Optional<Publicacion> publicacionExistente = publicacionRepo.findById(publicacion.getId());
        if (publicacionExistente.isPresent()) {
            throw new Exception("El email ya se encuentra en uso");
        }
        return publicacionRepo.save(publicacion);
    }

    @Override
    public Publicacion actualizarPublicacion(Publicacion publicacion) throws Exception {
        Optional<Publicacion> publicacionExistente = publicacionRepo.findById(publicacion.getId());
        if (publicacionExistente.isEmpty()) {
            throw new Exception("No existe una publicación con ese id");
        }
        return publicacionRepo.save(publicacion);
    }

    @Override
    public Publicacion obtenerPublicacion(Long idPublicacion) throws Exception {
        Optional<Publicacion> publicacion = publicacionRepo.findById(idPublicacion);
        if (publicacion.isEmpty()) {
            throw new Exception("No existe una publicación con ese id");
        }
        return publicacion.get();
    }

    @Override
    public void eliminarPublicacion(Long idPublicacion) throws Exception {
        Optional<Publicacion> publicacion = publicacionRepo.findById(idPublicacion);
        if (publicacion.isEmpty()) {
            throw new Exception("No existe una publicación con ese id");
        }
        publicacionRepo.delete(publicacion.get());
    }

    @Override
    public List<Publicacion> listarPublicaciones() {
        return publicacionRepo.findAll();
    }

    @Override
    public List<Publicacion> buscarPublicacionPorFrase(String frase) {
        return publicacionRepo.obtenerPublicacionesPorFrase(frase);
    }

}
