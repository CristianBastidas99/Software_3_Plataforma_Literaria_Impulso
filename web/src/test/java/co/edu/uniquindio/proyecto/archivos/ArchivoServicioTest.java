package co.edu.uniquindio.proyecto.archivos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static co.edu.uniquindio.proyecto.archivos.ArchivoServicio.Tipo.IMAGEN;
import static co.edu.uniquindio.proyecto.archivos.ArchivoServicio.Tipo.PDF;
import static org.junit.jupiter.api.Assertions.*;

class ArchivoServicioTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1, 1};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};
    private static final byte[] PDF_BYTES = "%PDF-1.4\n%contenido de prueba\n".getBytes();
    private static final byte[] EXE = {'M', 'Z', (byte) 0x90, 0, 3, 0, 0, 0, 4, 0, 0, 0, (byte) 0xFF, (byte) 0xFF};

    @TempDir
    Path temporal;

    private Path base;
    private ArchivoServicio servicio;

    @BeforeEach
    void preparar() throws Exception {
        base = temporal.resolve("subidas");
        servicio = new ArchivoServicio(base.toString(), 1, 1);
    }

    private String guardar(String nombre, byte[] datos, ArchivoServicio.Tipo tipo) throws Exception {
        return servicio.guardar(nombre, datos.length, new ByteArrayInputStream(datos), tipo);
    }

    private Path enDisco(String ruta) {
        return base.resolve(ruta.substring(ArchivoServicio.PREFIJO.length()));
    }

    @Test
    void creaLasCarpetasAlIniciar() {
        assertTrue(Files.isDirectory(base.resolve("img")));
        assertTrue(Files.isDirectory(base.resolve("pdf")));
    }

    @Test
    void guardaImagenesValidasConNombreUuid() throws Exception {
        String png = guardar("mi foto.PNG", PNG, IMAGEN);
        String jpg = guardar("retrato.jpeg", JPG, IMAGEN);
        String webp = guardar("portada.webp", WEBP, IMAGEN);

        assertTrue(png.matches("uploads/img/[0-9a-f-]{36}\\.png"), png);
        assertTrue(jpg.matches("uploads/img/[0-9a-f-]{36}\\.jpg"), jpg);
        assertTrue(webp.matches("uploads/img/[0-9a-f-]{36}\\.webp"), webp);
        assertArrayEquals(PNG, Files.readAllBytes(enDisco(png)));
        assertNotEquals(png, guardar("mi foto.PNG", PNG, IMAGEN), "cada subida debe tener un nombre distinto");
    }

    @Test
    void guardaPdfValido() throws Exception {
        String ruta = guardar("capitulo1.pdf", PDF_BYTES, PDF);
        assertTrue(ruta.matches("uploads/pdf/[0-9a-f-]{36}\\.pdf"), ruta);
        assertArrayEquals(PDF_BYTES, Files.readAllBytes(enDisco(ruta)));
    }

    @Test
    void rechazaEjecutableRenombradoAJpg() {
        ArchivoInvalidoException e = assertThrows(ArchivoInvalidoException.class, () -> guardar("virus.jpg", EXE, IMAGEN));
        assertTrue(e.getMessage().contains("no corresponde"), e.getMessage());
        assertEquals(0, archivosEn(base.resolve("img")));
    }

    @Test
    void rechazaPdfDisfrazadoDeImagenEImagenDisfrazadaDePdf() {
        assertThrows(ArchivoInvalidoException.class, () -> guardar("foto.png", PDF_BYTES, IMAGEN));
        assertThrows(ArchivoInvalidoException.class, () -> guardar("libro.pdf", PNG, PDF));
    }

    @Test
    void rechazaExtensionesNoPermitidas() {
        for (String nombre : Arrays.asList("animacion.gif", "programa.exe", "sin_extension", "imagen.jpg.exe", "")) {
            ArchivoInvalidoException e = assertThrows(ArchivoInvalidoException.class, () -> guardar(nombre, PNG, IMAGEN), nombre);
            assertTrue(e.getMessage().startsWith("Formato no permitido"), e.getMessage());
        }
        assertThrows(ArchivoInvalidoException.class, () -> guardar(null, PNG, IMAGEN));
        assertThrows(ArchivoInvalidoException.class, () -> guardar("documento.docx", PDF_BYTES, PDF));
    }

    @Test
    void nombreConPathTraversalNoEscribeFueraDeLaCarpeta() throws Exception {
        String ruta = guardar("../../hack.jpg", JPG, IMAGEN);
        String ruta2 = guardar("..\\..\\..\\hack.jpg", JPG, IMAGEN);

        assertTrue(ruta.matches("uploads/img/[0-9a-f-]{36}\\.jpg"), ruta);
        assertTrue(ruta2.matches("uploads/img/[0-9a-f-]{36}\\.jpg"), ruta2);
        assertTrue(enDisco(ruta).normalize().startsWith(base.resolve("img")));
        try (Stream<Path> todos = Files.walk(temporal)) {
            assertTrue(todos.noneMatch(p -> p.getFileName().toString().contains("hack")), "no debe existir ningún archivo 'hack'");
        }
    }

    @Test
    void rechazaArchivosQueSuperanElLimite() {
        byte[] grande = Arrays.copyOf(PNG, 1024 * 1024 + 1);

        ArchivoInvalidoException declarado = assertThrows(ArchivoInvalidoException.class, () -> guardar("grande.png", grande, IMAGEN));
        assertTrue(declarado.getMessage().contains("tamaño máximo de 1 MB"), declarado.getMessage());

        // El cliente declara un tamaño pequeño pero envía más: se detecta al copiar y no queda el archivo a medias
        assertThrows(ArchivoInvalidoException.class,
                () -> servicio.guardar("grande.png", 10, new ByteArrayInputStream(grande), IMAGEN));
        assertEquals(0, archivosEn(base.resolve("img")));
    }

    @Test
    void eliminaSoloArchivosSubidosDentroDeLaCarpeta() throws Exception {
        String ruta = guardar("foto.png", PNG, IMAGEN);
        Path fuera = Files.write(temporal.resolve("importante.txt"), new byte[]{1});

        assertFalse(servicio.eliminar("demo_portada_niebla.jpg"), "los recursos estáticos no se tocan");
        assertFalse(servicio.eliminar("uploads/../importante.txt"));
        assertFalse(servicio.eliminar(null));
        assertTrue(Files.exists(fuera));

        assertTrue(servicio.eliminar(ruta));
        assertFalse(Files.exists(enDisco(ruta)));
    }

    private static long archivosEn(Path carpeta) {
        try (Stream<Path> archivos = Files.list(carpeta)) {
            return archivos.count();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
