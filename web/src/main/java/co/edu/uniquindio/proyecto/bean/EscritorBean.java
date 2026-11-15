package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Escritor;
import co.edu.uniquindio.proyecto.entidades.Publicacion;
import co.edu.uniquindio.proyecto.servicios.EscritorServicio;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@ViewScoped
@Getter
@Setter
public class EscritorBean implements Serializable {

    @Autowired
    private EscritorServicio escritorServicio;
    @Value("#{param['idEscr']}")
    private String idEscritor;

    private List<ResponsiveOption> responsiveOptions;
    private Escritor escritor;
    private List<Publicacion> publicacionList;
    /** Indica si el escritor solicitado existe; si es falso la vista muestra un mensaje en lugar del perfil. */
    private boolean encontrado;

    @PostConstruct
    public void inicializar() {
        publicacionList = new ArrayList<>();
        encontrado = false;

        Long id = ParametrosUrl.aLong(idEscritor);
        if (id != null) {
            try {
                escritor = escritorServicio.obtenerEscritor(id);
                publicacionList = escritorServicio.obtenerPublicacionesDeEscritor(id);
                encontrado = true;
            } catch (Exception e) {
                escritor = null;
            }
        }

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 3, 3));
        responsiveOptions.add(new ResponsiveOption("768px", 2, 2));
        responsiveOptions.add(new ResponsiveOption("560px", 1, 1));
    }
}
