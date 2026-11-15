package co.edu.uniquindio.proyecto.archivos;

import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Guarda las imágenes y PDFs que suben los usuarios en una carpeta externa a la aplicación
 * ({@code upload.dir}), que se sirve por HTTP en {@code /uploads/**}.
 *
 * <ul>
 *   <li>El tipo se valida por extensión <b>y</b> por contenido (firma de bytes), así que un
 *       ejecutable renombrado a {@code .jpg} se rechaza.</li>
 *   <li>El tamaño se comprueba con lo declarado y también mientras se copia.</li>
 *   <li>El archivo se guarda con un nombre UUID; el nombre que envía el cliente nunca forma
 *       parte de la ruta, lo que evita el <i>path traversal</i>.</li>
 * </ul>
 *
 * <p>Los métodos {@code guardar*} devuelven la ruta relativa que se guarda en la base de datos,
 * por ejemplo {@code uploads/img/3f2a...e1.jpg}, que coincide con su URL pública.</p>
 */
@Service
public class ArchivoServicio {

    /** Prefijo de las rutas guardadas en la base de datos y de la URL pública. */
    public static final String PREFIJO = "uploads/";

    private static final Logger LOG = LoggerFactory.getLogger(ArchivoServicio.class);
    private static final long MB = 1024L * 1024L;

    /** Tipos de archivo admitidos, con su carpeta, extensiones y firmas válidas. */
    public enum Tipo {
        IMAGEN("img", "La imagen", Arrays.asList("jpg", "jpeg", "png", "webp"), "JPG, PNG o WEBP"),
        PDF("pdf", "El PDF", Arrays.asList("pdf"), "PDF");

        private final String carpeta;
        private final String nombre;
        private final List<String> extensiones;
        private final String formatos;

        Tipo(String carpeta, String nombre, List<String> extensiones, String formatos) {
            this.carpeta = carpeta;
            this.nombre = nombre;
            this.extensiones = extensiones;
            this.formatos = formatos;
        }

        public String getCarpeta() {
            return carpeta;
        }
    }

    private final Path directorioBase;
    private final long maxImagenBytes;
    private final long maxPdfBytes;

    public ArchivoServicio(@Value("${upload.dir:uploads}") String directorio,
                           @Value("${upload.imagen.max-mb:5}") long maxImagenMb,
                           @Value("${upload.pdf.max-mb:20}") long maxPdfMb) throws IOException {
        this.directorioBase = Paths.get(directorio).toAbsolutePath().normalize();
        this.maxImagenBytes = maxImagenMb * MB;
        this.maxPdfBytes = maxPdfMb * MB;
        for (Tipo tipo : Tipo.values()) {
            Files.createDirectories(directorioBase.resolve(tipo.carpeta));
        }
        LOG.info("Carpeta de archivos subidos: {}", directorioBase);
    }

    public String guardarImagen(UploadedFile archivo) throws ArchivoInvalidoException, IOException {
        return guardar(archivo, Tipo.IMAGEN);
    }

    public String guardarPdf(UploadedFile archivo) throws ArchivoInvalidoException, IOException {
        return guardar(archivo, Tipo.PDF);
    }

    private String guardar(UploadedFile archivo, Tipo tipo) throws ArchivoInvalidoException, IOException {
        if (archivo == null) {
            throw new ArchivoInvalidoException("No se recibió ningún archivo");
        }
        try (InputStream entrada = archivo.getInputStream()) {
            return guardar(archivo.getFileName(), archivo.getSize(), entrada, tipo);
        }
    }

    /**
     * Valida y guarda un archivo.
     *
     * @param nombreOriginal nombre enviado por el cliente; solo se usa para comprobar la extensión
     * @param tamanoDeclarado tamaño informado por el cliente (se vuelve a comprobar al copiar)
     * @return la ruta relativa a guardar en la base de datos (por ejemplo {@code uploads/img/uuid.png})
     */
    public String guardar(String nombreOriginal, long tamanoDeclarado, InputStream entrada, Tipo tipo)
            throws ArchivoInvalidoException, IOException {
        String extension = extension(nombreOriginal);
        if (!tipo.extensiones.contains(extension)) {
            throw new ArchivoInvalidoException("Formato no permitido. " + tipo.nombre + " debe ser " + tipo.formatos + ".");
        }
        long maximo = maximo(tipo);
        if (tamanoDeclarado > maximo) {
            throw excesoTamano(tipo);
        }

        BufferedInputStream datos = new BufferedInputStream(entrada);
        datos.mark(16);
        byte[] cabecera = datos.readNBytes(12);
        datos.reset();
        String extensionReal = detectarExtension(cabecera, tipo);
        if (extensionReal == null) {
            throw new ArchivoInvalidoException("El contenido del archivo no corresponde a un archivo " + tipo.formatos + ".");
        }

        String nombre = UUID.randomUUID() + "." + extensionReal;
        Path destino = directorioBase.resolve(tipo.carpeta).resolve(nombre).normalize();
        if (!destino.startsWith(directorioBase)) {
            throw new ArchivoInvalidoException("Ruta de destino no válida");
        }

        copiarConLimite(datos, destino, maximo, tipo);
        return PREFIJO + tipo.carpeta + "/" + nombre;
    }

    /**
     * Elimina un archivo subido. Ignora las rutas que no son de subidas (por ejemplo los recursos de la demo).
     *
     * @return {@code true} si se eliminó un archivo
     */
    public boolean eliminar(String ruta) {
        if (ruta == null || !ruta.startsWith(PREFIJO)) {
            return false;
        }
        Path archivo = directorioBase.resolve(ruta.substring(PREFIJO.length())).normalize();
        if (!archivo.startsWith(directorioBase)) {
            return false;
        }
        try {
            return Files.deleteIfExists(archivo);
        } catch (IOException e) {
            LOG.warn("No se pudo eliminar el archivo {}", archivo, e);
            return false;
        }
    }

    public Path getDirectorioBase() {
        return directorioBase;
    }

    public long getMaxImagenBytes() {
        return maxImagenBytes;
    }

    public long getMaxPdfBytes() {
        return maxPdfBytes;
    }

    private long maximo(Tipo tipo) {
        return tipo == Tipo.IMAGEN ? maxImagenBytes : maxPdfBytes;
    }

    private ArchivoInvalidoException excesoTamano(Tipo tipo) {
        return new ArchivoInvalidoException(tipo.nombre + " supera el tamaño máximo de " + (maximo(tipo) / MB) + " MB.");
    }

    private void copiarConLimite(InputStream entrada, Path destino, long maximo, Tipo tipo)
            throws IOException, ArchivoInvalidoException {
        long total = 0;
        byte[] bufer = new byte[8192];
        try (OutputStream salida = Files.newOutputStream(destino)) {
            int leidos;
            while ((leidos = entrada.read(bufer)) != -1) {
                total += leidos;
                if (total > maximo) {
                    throw excesoTamano(tipo);
                }
                salida.write(bufer, 0, leidos);
            }
        } catch (IOException | ArchivoInvalidoException e) {
            Files.deleteIfExists(destino);
            throw e;
        }
    }

    private static String extension(String nombre) {
        if (nombre == null) {
            return "";
        }
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    /** Devuelve la extensión que corresponde a la firma de bytes, o {@code null} si no es un tipo admitido. */
    private static String detectarExtension(byte[] c, Tipo tipo) {
        if (tipo == Tipo.IMAGEN) {
            if (empiezaCon(c, 0xFF, 0xD8, 0xFF)) {
                return "jpg";
            }
            if (empiezaCon(c, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
                return "png";
            }
            if (empiezaCon(c, 'R', 'I', 'F', 'F') && c.length >= 12
                    && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') {
                return "webp";
            }
            return null;
        }
        return empiezaCon(c, '%', 'P', 'D', 'F', '-') ? "pdf" : null;
    }

    private static boolean empiezaCon(byte[] datos, int... firma) {
        if (datos.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
