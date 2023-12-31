package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorServicioImpl implements AdministradorServicio{

    private final AdministradorRepo administradorRepo;

    public AdministradorServicioImpl(AdministradorRepo administradorRepo) {
        this.administradorRepo = administradorRepo;
    }

    @Override
    public Administrador registrarAdministrador(Administrador administrador) throws Exception{
        Optional<Administrador> administrador1 = administradorRepo.buscarAdministradorPorCorreo(administrador.getEmail());
        if(administrador1.isPresent()){
            throw new Exception("El email ya se encuentra en uso");
        }
        return administradorRepo.save(administrador);
    }

    @Override
    public Administrador actualizarAdministrador(Administrador administrador) throws Exception {
        Optional<Administrador> administrador1 = administradorRepo.findById(administrador.getId());
        if(administrador1.isEmpty()){
            throw new Exception("No exite un administrador con ese id");
        }
        return administradorRepo.save(administrador);
    }

    @Override
    public Administrador obtenerAdministrador(Long idAdministrador) throws Exception {
        Optional<Administrador> administrador = administradorRepo.findById(idAdministrador);
        if(administrador.isEmpty()){
            throw new Exception("No exite un administrador con ese id");
        }
        return administrador.get();
    }

    @Override
    public void eliminarAdministrador(Long idAdministrador) throws Exception {
        Optional<Administrador> administrador = administradorRepo.findById(idAdministrador);
        if(administrador.isEmpty()){
            throw new Exception("No exite un administrador con ese id");
        }
        administradorRepo.delete(administrador.get());
    }

    @Override
    public List<Administrador> listarAdministradores() {
        return administradorRepo.findAll();
    }

    @Override
    public Administrador validaLogin(String email, String contrasena) throws Exception {
        Optional<Administrador> administrador = administradorRepo.iniciarSesionAdministrador(email, contrasena);

        if (administrador.isEmpty()) {
            return null;
        }

        Administrador admin = administrador.get();

        if (admin.getEstado() == Estado.INACTIVO) {
            throw new Exception("El administrador está INACTIVO");
        }

        return admin;
    }


}
