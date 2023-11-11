package co.edu.uniquindio.proyecto.servicios;
import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FragmentoServicioImpl implements FragmentoServicio{

    private final FragmentoRepo fragmentoRepo;

    public FragmentoServicioImpl(FragmentoRepo fragmentoRepo) {
        this.fragmentoRepo = fragmentoRepo;
    }

    @Override
    public Fragmento registrarFragmento(Fragmento fragmento) throws Exception{
        Optional<Fragmento> fragmentoExistente = fragmentoRepo.findById(fragmento.getId());
        if(fragmentoExistente.isPresent()){
            throw new Exception("No existe un fragmento con ese id");
        }
        return fragmentoRepo.save(fragmento);
    }

    @Override
    public Fragmento actualizarFragmento(Fragmento fragmento) throws Exception {
        Optional<Fragmento> fragmentoExistente = fragmentoRepo.findById(fragmento.getId());
        if(fragmentoExistente.isEmpty()){
            throw new Exception("No existe un fragmento con ese id");
        }
        return fragmentoRepo.save(fragmento);
    }

    @Override
    public Fragmento obtenerFragmento(Long idFragmento) throws Exception {
        Optional<Fragmento> fragmento = fragmentoRepo.findById(idFragmento);
        if(fragmento.isEmpty()){
            throw new Exception("No existe un fragmento con ese id");
        }
        return fragmento.get();
    }

    @Override
    public void eliminarFragmento(Long idFragmento) throws Exception {
        Optional<Fragmento> fragmento = fragmentoRepo.findById(idFragmento);
        if(fragmento.isEmpty()){
            throw new Exception("No existe un fragmento con ese id");
        }
        fragmentoRepo.delete(fragmento.get());
    }

    @Override
    public List<Fragmento> listarFragmentos() {
        return fragmentoRepo.findAll();
    }


}
