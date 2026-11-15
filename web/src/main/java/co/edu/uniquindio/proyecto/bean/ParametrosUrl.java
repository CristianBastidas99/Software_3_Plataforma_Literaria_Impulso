package co.edu.uniquindio.proyecto.bean;

/**
 * Conversión segura de parámetros recibidos por URL.
 */
final class ParametrosUrl {

    private ParametrosUrl() {
    }

    /**
     * Convierte el parámetro a un id positivo.
     *
     * @return el id, o {@code null} si el parámetro está vacío, no es numérico o no es positivo
     */
    static Long aLong(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(valor.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
