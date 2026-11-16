package co.edu.uniquindio.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Totales que se muestran en el panel del administrador. */
@Getter
@Setter
public class Estadisticas implements Serializable {

    private long escritoresActivos;
    private long escritoresBloqueados;
    private long lectoresActivos;
    private long lectoresBloqueados;
    private long obras;
    private long publicacionesPublicadas;
    private long publicacionesOcultas;
    private long publicacionesRetiradas;
    private long comentarios;
    private long comentariosOcultos;
    private List<PublicacionVistas> masVistas = new ArrayList<>();

    public long getEscritores() {
        return escritoresActivos + escritoresBloqueados;
    }

    public long getLectores() {
        return lectoresActivos + lectoresBloqueados;
    }

    /** Una fila del ranking de publicaciones más vistas. */
    @Getter
    @AllArgsConstructor
    public static class PublicacionVistas implements Serializable {
        private final Long id;
        private final String titulo;
        private final long vistas;
    }
}
