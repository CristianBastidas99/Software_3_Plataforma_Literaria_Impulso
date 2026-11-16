package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EscritorServicioImpl implements EscritorServicio{

    private final EscritorRepo escritorRepo;

    public EscritorServicioImpl(EscritorRepo escritorRepo) {
        this.escritorRepo = escritorRepo;
    }

    @Override
    public Escritor registrarEscritor(Escritor escritor) throws Exception{
        Optional<Escritor> escritorExistente = escritorRepo.buscarEscritorPorCorreo(escritor.getEmail());
        if(escritorExistente.isPresent()){
            throw new Exception("El email ya se encuentra en uso");
        }
        return escritorRepo.save(escritor);
    }

    @Override
    public Escritor actualizarEscritor(Escritor escritor) throws Exception {
        Optional<Escritor> escritorExistente = escritorRepo.findById(escritor.getId());
        if(escritorExistente.isEmpty()){
            throw new Exception("No existe un escritor con ese id");
        }
        return escritorRepo.save(escritor);
    }

    @Override
    public Escritor obtenerEscritor(Long idEscritor) throws Exception {
        Optional<Escritor> escritor = escritorRepo.findById(idEscritor);
        if(escritor.isEmpty()){
            throw new Exception("No existe un escritor con ese id");
        }
        return escritor.get();
    }

    @Override
    public void eliminarEscritor(Long idEscritor) throws Exception {
        Optional<Escritor> escritor = escritorRepo.findById(idEscritor);
        if(escritor.isEmpty()){
            throw new Exception("No existe un escritor con ese id");
        }
        escritorRepo.delete(escritor.get());
    }

    @Override
    public List<Escritor> listarEscritores() {
        return escritorRepo.findAll();
    }

    @Override
    public List<Escritor> buscarEscritoresPorFrase(String frase) {
        return escritorRepo.obtenerEscritoresPorFrase(frase);
    }

    @Override
    public List<Publicacion> obtenerPublicacionesDeEscritor(Long idEscritor) throws Exception {
        Optional<Escritor> escritor = escritorRepo.findById(idEscritor);
        if(escritor.isEmpty()){
            throw new Exception("No existe un escritor con ese id");
        }
        return escritorRepo.obtenerPublicacionesAprobadasDeEscritor(idEscritor);
    }

    @Override
    public Escritor validaLogin(String email, String contrasena) throws Exception {
        Optional<Escritor> escritor = escritorRepo.buscarEscritorPorCorreo(email)
                .filter(u -> Contrasenas.coincide(contrasena, u.getPassword()));

        if (escritor.isEmpty()) {
            return null;
        }

        Escritor escritorValido = escritor.get();

        if (escritorValido.getEstado() == Estado.INACTIVO) {
            throw new Exception(LoginServicio.CUENTA_BLOQUEADA);
        }

        return escritorValido;
    }


}
