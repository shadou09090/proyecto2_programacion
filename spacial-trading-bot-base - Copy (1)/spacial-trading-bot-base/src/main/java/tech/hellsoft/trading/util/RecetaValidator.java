package tech.hellsoft.trading.util;

import java.util.Map;
import tech.hellsoft.trading.modelo.Receta;

public final class RecetaValidator {

    private RecetaValidator() {}

    /**
     * Verifica si el inventario tiene todos los ingredientes necesarios
     * para producir el producto definido por la receta.
     */
    public static boolean puedeProducir(Receta receta, Map<String, Integer> inventario) {
        if (receta == null || inventario == null) {
            return false;
        }

        Map<String, Integer> req = receta.ingredientes();

        // Receta sin ingredientes → siempre producible
        if (req == null || req.isEmpty()) {
            return true;
        }

        // Verificar uno por uno
        for (var entry : req.entrySet()) {
            String ingrediente = entry.getKey();
            int requerido = entry.getValue();

            // Cantidades negativas son inválidas → receta corrupta
            if (requerido < 0) {
                return false;
            }

            int disponible = inventario.getOrDefault(ingrediente, 0);

            if (disponible < requerido) {
                return false;
            }
        }

        return true;
    }

    /**
     * Consume los ingredientes requeridos por la receta.
     * Debe llamarse SOLO si puedeProducir(receta, inventario) es true.
     */
    public static void consumirIngredientes(Receta receta, Map<String, Integer> inventario) {
        if (receta == null || inventario == null) {
            return;
        }

        Map<String, Integer> req = receta.ingredientes();

        if (req == null || req.isEmpty()) {
            return; // nada que consumir
        }

        req.forEach((ingrediente, requerido) -> {
            int disponible = inventario.getOrDefault(ingrediente, 0);
            int nuevo = disponible - requerido;

            // Seguridad: nunca permitir números negativos
            inventario.put(ingrediente, Math.max(nuevo, 0));
        });
    }
}
