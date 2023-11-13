package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LectorServicioImpl implements LectorServicio{

    private final LectorRepo lectorRepo;

    public LectorServicioImpl(LectorRepo lectorRepo) {
        this.lectorRepo = lectorRepo;
    }

    @Override
    public Lector registrarLector(Lector lector) throws Exception {
        Optional<Lector> lectorExistente = lectorRepo.buscarLectorPorCorreo(lector.getEmail());
        if (lectorExistente.isPresent()) {
            throw new Exception("El email ya se encuentra en uso");
        }
        return lectorRepo.save(lector);
    }

    @Override
    public Lector actualizarLector(Lector lector) throws Exception {
        Optional<Lector> lectorExistente = lectorRepo.findById(lector.getId());
        if (lectorExistente.isEmpty()) {
            throw new Exception("No existe un lector con ese id");
        }
        return lectorRepo.save(lector);
    }

    @Override
    public Lector obtenerLector(Long idLector) throws Exception {
        Optional<Lector> lector = lectorRepo.findById(idLector);
        if (lector.isEmpty()) {
            throw new Exception("No existe un lector con ese id");
        }
        return lector.get();
    }

    @Override
    public void eliminarLector(Long idLector) throws Exception {
        Optional<Lector> lector = lectorRepo.findById(idLector);
        if (lector.isEmpty()) {
            throw new Exception("No existe un lector con ese id");
        }
        lectorRepo.delete(lector.get());
    }

    @Override
    public List<Lector> listarLectores() {
        return lectorRepo.findAll();
    }

    @Override
    public Lector validaLogin(String email, String contrasena) throws Exception {
        Optional<Lector> lector = lectorRepo.iniciarSesionLector(email, contrasena);

        if (lector.isEmpty()) {
            return null;
        }

        Lector lectorValido = lector.get();

        if (lectorValido.getEstado() == Estado.INACTIVO) {
            throw new Exception("El lector está INACTIVO");
        }

        return lectorValido;
    }

}
