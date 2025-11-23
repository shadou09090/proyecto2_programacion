package tech.hellsoft.trading.util;

import java.util.Map;
import tech.hellsoft.trading.modelo.Receta;

public final class RecetaValidator {

  private RecetaValidator() {
  }

  public static boolean puedeProducir(Receta receta, Map<String, Integer> inventario) {
    if (receta == null || inventario == null) {
      return false;
    }
    return receta.ingredientes().entrySet().stream()
        .allMatch(entry -> inventario.getOrDefault(entry.getKey(), 0) >= entry.getValue());
  }
}
