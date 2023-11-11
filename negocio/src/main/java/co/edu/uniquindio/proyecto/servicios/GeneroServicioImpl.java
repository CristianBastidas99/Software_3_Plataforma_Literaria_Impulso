package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GeneroServicioImpl implements  GeneroServicio{

    private final GeneroRepo generoRepo;

    public GeneroServicioImpl(GeneroRepo generoRepo) {
        this.generoRepo = generoRepo;
    }

    @Override
    public Genero registrarGenero(Genero genero) throws Exception{
        Optional<Genero> generoExistente = generoRepo.findById(genero.getId());
        if(generoExistente.isPresent()){
            throw new Exception("No existe un genero con ese id");
        }
        return generoRepo.save(genero);
    }

    @Override
    public Genero actualizarGenero(Genero genero) throws Exception {
        Optional<Genero> generoExistente = generoRepo.findById(genero.getId());
        if(generoExistente.isEmpty()){
            throw new Exception("No existe un genero con ese id");
        }
        return generoRepo.save(genero);
    }

    @Override
    public Genero obtenerGenero(Long idGenero) throws Exception {
        Optional<Genero> genero = generoRepo.findById(idGenero);
        if(genero.isEmpty()){
            throw new Exception("No existe un genero con ese id");
        }
        return genero.get();
    }

    @Override
    public void eliminarGenero(Long idGenero) throws Exception {
        Optional<Genero> genero = generoRepo.findById(idGenero);
        if(genero.isEmpty()){
            throw new Exception("No existe un genero con ese id");
        }
        generoRepo.delete(genero.get());
    }

    @Override
    public List<Genero> listarGeneros() {
        return generoRepo.findAll();
    }

}
