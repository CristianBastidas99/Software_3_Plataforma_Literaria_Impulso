package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObraLiterariaServicioImpl implements ObraLiterariaServicio{

    private final ObraLiterariaRepo obraLiterariaRepo;

    public ObraLiterariaServicioImpl(ObraLiterariaRepo obraLiterariaRepo) {
        this.obraLiterariaRepo = obraLiterariaRepo;
    }

    @Override
    public ObraLiteraria registrarObraLiteraria(ObraLiteraria obraLiteraria) throws Exception {
        Optional<ObraLiteraria> obraLiterariaExistente = obraLiterariaRepo.findById(obraLiteraria.getId());
        if (obraLiterariaExistente.isPresent()) {
            throw new Exception("No existe una obraLiteraria con ese id");
        }
        return obraLiterariaRepo.save(obraLiteraria);
    }

    @Override
    public ObraLiteraria actualizarObraLiteraria(ObraLiteraria obraLiteraria) throws Exception {
        Optional<ObraLiteraria> obraLiterariaExistente = obraLiterariaRepo.findById(obraLiteraria.getId());
        if (obraLiterariaExistente.isEmpty()) {
            throw new Exception("No existe una obra literaria con ese id");
        }
        return obraLiterariaRepo.save(obraLiteraria);
    }

    @Override
    public ObraLiteraria obtenerObraLiteraria(Long idObraLiteraria) throws Exception {
        Optional<ObraLiteraria> obraLiteraria = obraLiterariaRepo.findById(idObraLiteraria);
        if (obraLiteraria.isEmpty()) {
            throw new Exception("No existe una obra literaria con ese id");
        }
        return obraLiteraria.get();
    }

    @Override
    public void eliminarObraLiteraria(Long idObraLiteraria) throws Exception {
        Optional<ObraLiteraria> obraLiteraria = obraLiterariaRepo.findById(idObraLiteraria);
        if (obraLiteraria.isEmpty()) {
            throw new Exception("No existe una obra literaria con ese id");
        }
        obraLiterariaRepo.delete(obraLiteraria.get());
    }

    @Override
    public List<ObraLiteraria> listarObrasLiterarias() {
        return obraLiterariaRepo.findAll();
    }

}
