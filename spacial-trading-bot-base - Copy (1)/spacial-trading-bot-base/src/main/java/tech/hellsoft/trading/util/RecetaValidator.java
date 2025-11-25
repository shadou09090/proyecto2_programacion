package tech.hellsoft.trading.util;

import java.util.Map;
import tech.hellsoft.trading.modelo.Receta;

public final class RecetaValidator {

    private RecetaValidator() {
    }

    /**
     Verifica si el inventario tiene todos los ingredientes necesarios
     para producir el producto definido por la receta*/
    public static boolean puedeProducir(Receta receta, Map<String, Integer> inventario) {
        if (receta == null || inventario == null) {
            return false;
        }

        // Si no tiene ingredientes → es una receta básica → se puede producir
        if (receta.getIngredientes() == null || receta.getIngredientes().isEmpty()) {
            return true;
        }

        // Verificar uno por uno
        return receta.getIngredientes().entrySet().stream()
                .allMatch(entry ->
                        inventario.getOrDefault(entry.getKey(), 0) >= entry.getValue()
                );
    }

    /**
     * Consume los ingredientes requeridos por la receta.
     * Debe llamarse SOLO si puedeProducir(receta, inventario) es true.
     */
    public static void consumirIngredientes(Receta receta, Map<String, Integer> inventario) {
        if (receta == null || inventario == null) {
            return;
        }

        if (receta.getIngredientes() == null || receta.getIngredientes().isEmpty()) {
            return; // receta sin ingredientes → nada que consumir
        }

        receta.getIngredientes().forEach((ingrediente, requerido) -> {
            int disponible = inventario.getOrDefault(ingrediente, 0);
            inventario.put(ingrediente, disponible - requerido);
        });
    }
}
