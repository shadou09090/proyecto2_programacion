import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tech.hellsoft.trading.config.Configuration;
import tech.hellsoft.trading.exception.ConfiguracionInvalidaException;
import tech.hellsoft.trading.modelo.Receta;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class ConfigLoader {

    private static final Gson GSON = new Gson();

    private ConfigLoader() {
    }

    //Lee config.json y lo convierte en objeto Configuration//
    public static Configuration cargarConfig(String archivo)
            throws ConfiguracionInvalidaException {

        String json = leerArchivo(archivo);

        Configuration cfg;
        try {
            cfg = GSON.fromJson(json, Configuration.class);
        } catch (Exception e) {
            throw new ConfiguracionInvalidaException("JSON de configuración inválido", e);
        }

        // Validar campos obligatorios
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            throw new ConfiguracionInvalidaException("apiKey faltante");
        }
        if (cfg.getEquipo() == null || cfg.getEquipo().isBlank()) {
            throw new ConfiguracionInvalidaException("equipo faltante");
        }
        if (cfg.getHost() == null || cfg.getHost().isBlank()) {
            throw new ConfiguracionInvalidaException("host faltante");
        }

        return cfg;
    }

    //Carga recetas desde un archivo JSON y las convierte en un Map<String, Receta>//
    public static Map<String, Receta> cargarRecetas(String archivo)
            throws ConfiguracionInvalidaException {

        String json = leerArchivo(archivo);

        try {
            Type type = new TypeToken<Map<String, Receta>>() {}.getType();
            return GSON.fromJson(json, type);
        } catch (Exception e) {
            throw new ConfiguracionInvalidaException("JSON de recetas inválido", e);
        }
    }

    // Lee cualquier archivo de texto y devuelve su contenido//
    private static String leerArchivo(String archivo)
            throws ConfiguracionInvalidaException {

        if (archivo == null || archivo.isBlank()) {
            throw new ConfiguracionInvalidaException("Ruta vacía");
        }

        Path path = Paths.get(archivo);

        if (!Files.exists(path)) {
            throw new ConfiguracionInvalidaException("No existe: " + archivo);
        }
        if (!Files.isReadable(path)) {
            throw new ConfiguracionInvalidaException("No se puede leer: " + archivo);
        }

        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new ConfiguracionInvalidaException("Error leyendo archivo: " + archivo, e);
        }
    }
}
