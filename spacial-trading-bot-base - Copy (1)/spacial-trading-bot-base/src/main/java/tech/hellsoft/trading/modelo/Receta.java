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

  public Map<String, Integer> ingredientes() {
    return Collections.unmodifiableMap(ingredientes);
  }


        public static boolean puedeProducir(Receta receta, Map<String, Integer> inventario) {
            if (receta.getIngredientes() == null || receta.getIngredientes().isEmpty()) {
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

        public static void consumirIngredientes(Receta receta, Map<String, Integer> inventario) {
            for (Map.Entry<String, Integer> entry : receta.getIngredientes().entrySet()) {
                String ingrediente = entry.getKey();
                int requerido = entry.getValue();
                int disponible = inventario.getOrDefault(ingrediente, 0);
                inventario.put(ingrediente, disponible - requerido);
            }
        }
    }


