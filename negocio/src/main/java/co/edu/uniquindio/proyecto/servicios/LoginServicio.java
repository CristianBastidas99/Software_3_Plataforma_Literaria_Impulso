package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Lector;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginServicio {

    private final AdministradorRepo administradorRepo;
    private final EscritorRepo escritorRepo;
    private final LectorRepo lectorRepo;

    public LoginServicio(AdministradorRepo administradorRepo, EscritorRepo escritorRepo, LectorRepo lectorRepo) {
        this.administradorRepo = administradorRepo;
        this.escritorRepo = escritorRepo;
        this.lectorRepo = lectorRepo;
    }

    public boolean isEmailRegistrado(String emailString) throws Exception{
        Optional<Administrador> administrador = administradorRepo.buscarAdministradorPorCorreo(emailString);
        if(administrador.isPresent()){
            return true;
        }
        Optional<Escritor> escritor = escritorRepo.buscarEscritorPorCorreo(emailString);
        if(escritor.isPresent()){
            return true;
        }
        Optional<Lector> lector = lectorRepo.buscarLectorPorCorreo(emailString);
        if(lector.isPresent()){
            return true;
        }
        return false;
    }

}
