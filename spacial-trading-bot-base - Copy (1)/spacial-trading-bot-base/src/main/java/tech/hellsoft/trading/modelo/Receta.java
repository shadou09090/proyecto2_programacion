package tech.hellsoft.trading.modelo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Receta {

    private final Map<String, Integer> ingredientes = new HashMap<>();

    public Receta() {
    }

    public Receta(Map<String, Integer> ingredientes) {
        if (ingredientes != null) {
            this.ingredientes.putAll(ingredientes);
        }
    }

    /**
     * Devuelve los ingredientes de forma inmutable (para lectura externa).
     */
    public Map<String, Integer> ingredientes() {
        return Collections.unmodifiableMap(ingredientes);
    }

    /**
     * Acceso interno seguro para la clase (lectura directa).
     */
    public Map<String, Integer> getIngredientes() {
        return ingredientes;
    }

    //────────────────────────────────────────
    // LÓGICA DE PRODUCCIÓN
    //────────────────────────────────────────

    /**
     * Verifica si el inventario tiene suficientes ingredientes para esta receta.
     */
    public static boolean puedeProducir(Receta receta, Map<String, Integer> inventario) {
        if (receta == null || receta.getIngredientes().isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Integer> entry : receta.getIngredientes().entrySet()) {
            String ingrediente = entry.getKey();
            int requerido = entry.getValue();

            int disponible = inventario.getOrDefault(ingrediente, 0);
            if (disponible < requerido) {
                return false;
            }
        }
        return true;
    }

    /**
     * Descuenta del inventario los ingredientes utilizados en la receta.
     */
    public static void consumirIngredientes(Receta receta, Map<String, Integer> inventario) {
        if (receta == null) return;

        for (Map.Entry<String, Integer> entry : receta.getIngredientes().entrySet()) {
            String ingrediente = entry.getKey();
            int requerido = entry.getValue();

            int disponible = inventario.getOrDefault(ingrediente, 0);
            inventario.put(ingrediente, disponible - requerido);
        }
    }
}
