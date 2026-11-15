package co.edu.uniquindio.proyecto.servicios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtro de palabras ofensivas para los comentarios. Carga la lista de {@code malaspalabras.json}
 * al arrancar y censura con asteriscos las palabras que aparezcan en un texto.
 *
 * <p>La comparación es por palabra completa y no distingue mayúsculas ni tildes: "BOBALICON",
 * "bobalicón" y "Bobalicón" se censuran igual, pero "bobalicones" no.</p>
 */
@Service
public class FiltroPalabrasServicio {

    private static final Logger LOG = LoggerFactory.getLogger(FiltroPalabrasServicio.class);
    private static final String ARCHIVO = "malaspalabras.json";
    /** Una palabra: letras (con tildes y ñ) o dígitos seguidos. */
    private static final Pattern PALABRA = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern MARCAS = Pattern.compile("\\p{M}+");

    private final Set<String> palabras;

    public FiltroPalabrasServicio() {
        this.palabras = Collections.unmodifiableSet(cargar());
        LOG.info("Filtro de comentarios: {} palabras cargadas de {}", palabras.size(), ARCHIVO);
    }

    /** Devuelve el texto con cada palabra ofensiva sustituida por asteriscos de la misma longitud. */
    public String censurar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        Matcher m = PALABRA.matcher(texto);
        StringBuilder resultado = new StringBuilder(texto.length());
        int ultimo = 0;
        while (m.find()) {
            if (palabras.contains(normalizar(m.group()))) {
                resultado.append(texto, ultimo, m.start());
                resultado.append("*".repeat(m.group().length()));
                ultimo = m.end();
            }
        }
        resultado.append(texto.substring(ultimo));
        return resultado.toString();
    }

    public boolean contieneOfensivas(String texto) {
        if (texto == null) {
            return false;
        }
        Matcher m = PALABRA.matcher(texto);
        while (m.find()) {
            if (palabras.contains(normalizar(m.group()))) {
                return true;
            }
        }
        return false;
    }

    public int getTotalPalabras() {
        return palabras.size();
    }

    /** Minúsculas y sin tildes ni diéresis (la ñ se convierte en n, lo que es aceptable para comparar). */
    static String normalizar(String palabra) {
        String descompuesta = Normalizer.normalize(palabra.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return MARCAS.matcher(descompuesta).replaceAll("");
    }

    private static Set<String> cargar() {
        Set<String> conjunto = new HashSet<>();
        try (InputStream in = new ClassPathResource(ARCHIVO).getInputStream()) {
            JsonNode lista = new ObjectMapper().readTree(in).path("malasPalabras");
            for (JsonNode nodo : lista) {
                String palabra = nodo.asText("").trim();
                if (!palabra.isEmpty()) {
                    conjunto.add(normalizar(palabra));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        if (conjunto.isEmpty()) {
            throw new IllegalStateException(ARCHIVO + " no contiene palabras en \"malasPalabras\"");
        }
        return conjunto;
    }
}
