package tech.hellsoft.trading.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tech.hellsoft.trading.config.Configuration;
import tech.hellsoft.trading.exception.ConfiguracionInvalidaException;
import tech.hellsoft.trading.modelo.Receta;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class ConfigLoader {

    private static final Gson GSON = new Gson();

    private ConfigLoader() {}

     //METODO PRINCIPAL (compatibilidad con el proyecto)

    public static Configuration load(String archivo) throws ConfiguracionInvalidaException {
        return cargarConfig(archivo);
    }
    // CARGAR CONFIGURACIÓN
    public static Configuration cargarConfig(String archivo)
            throws ConfiguracionInvalidaException {

        String json = leerArchivo(archivo);

        Configuration cfg;
        try {
            cfg = GSON.fromJson(json, Configuration.class);
        } catch (Exception e) {
            throw new ConfiguracionInvalidaException("JSON de configuración inválido", e);
        }

        if (cfg == null) {
            throw new ConfiguracionInvalidaException("config.json vacío o inválido");
        }

        // Validar campos obligatorios
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            throw new ConfiguracionInvalidaException("apiKey faltante");
        }
        if (cfg.getTeam() == null || cfg.getTeam().isBlank()) {
            throw new ConfiguracionInvalidaException("team faltante");
        }
        if (cfg.getHost() == null || cfg.getHost().isBlank()) {
            throw new ConfiguracionInvalidaException("host faltante");
        }

        return cfg;
    }
    // CARGAR RECETAS JSON
    public static Map<String, Receta> cargarRecetas(String archivo)
            throws ConfiguracionInvalidaException {

        String json = leerArchivo(archivo);

        try {
            Type type = new TypeToken<Map<String, Receta>>(){}.getType();
            Map<String, Receta> recetas = GSON.fromJson(json, type);

            if (recetas == null) {
                throw new ConfiguracionInvalidaException("Archivo de recetas vacío");
            }

            return recetas;

        } catch (Exception e) {
            throw new ConfiguracionInvalidaException("JSON de recetas inválido", e);
        }
    }
    // LECTOR GENÉRICO
    private static String leerArchivo(String archivo)
            throws ConfiguracionInvalidaException {

        if (archivo == null || archivo.isBlank()) {
            throw new ConfiguracionInvalidaException("Ruta vacía");
        }

        // 1) Primero intentar cargar desde el filesystem
        Path path = Paths.get(archivo);

        if (Files.exists(path)) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                throw new ConfiguracionInvalidaException("Error leyendo archivo: " + archivo, e);
            }
        }

        // 2) Si no está en filesystem, intentar cargar desde resources
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(archivo)) {
            if (is != null) {
                return new String(is.readAllBytes());
            }
        } catch (IOException e) {
            throw new ConfiguracionInvalidaException("Error leyendo recurso: " + archivo, e);
        }

        throw new ConfiguracionInvalidaException(
                "No se encontró el archivo ni en filesystem ni en resources: " + archivo
        );
    }
}

