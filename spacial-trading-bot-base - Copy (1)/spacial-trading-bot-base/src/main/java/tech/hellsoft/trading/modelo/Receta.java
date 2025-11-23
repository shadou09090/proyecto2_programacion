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
}
