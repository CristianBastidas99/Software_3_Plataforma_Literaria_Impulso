package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComentarioServicioImpl implements ComentarioServicio{

    private final ComentarioRepo comentarioRepo;

    public ComentarioServicioImpl(ComentarioRepo comentarioRepo) {
        this.comentarioRepo = comentarioRepo;
    }

    @Override
    public Comentario registrarComentario(Comentario comentario) throws Exception {
        Optional<Comentario> comentario1 = comentarioRepo.findById(comentario.getId());
        if(comentario1.isPresent()){
            throw new Exception("El id ya se encuentra en uso");
        }else if(comentario.getEscritor()== null && comentario.getLector() == null){
            throw new Exception("El comentario debe tener un remitente");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario actualizarComentario(Comentario comentario) throws Exception {
        Optional<Comentario> comentarioExistente = comentarioRepo.findById(comentario.getId());
        if(comentarioExistente.isEmpty()){
            throw new Exception("No existe un comentario con ese id");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario obtenerComentario(Long idComentario) throws Exception {
        Optional<Comentario> comentario = comentarioRepo.findById(idComentario);
        if(comentario.isEmpty()){
            throw new Exception("No existe un comentario con ese id");
        }
        return comentario.get();
    }

    @Override
    public void eliminarComentario(Long idComentario) throws Exception {
        Optional<Comentario> comentario = comentarioRepo.findById(idComentario);
        if(comentario.isEmpty()){
            throw new Exception("No existe un comentario con ese id");
        }
        comentarioRepo.delete(comentario.get());
    }

    @Override
    public List<Comentario> listarComentario() {
        return comentarioRepo.findAll();
    }
}
