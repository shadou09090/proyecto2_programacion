package tech.hellsoft.trading.exception.ProduccionException;

import java.util.Map;

public class IngredientesInsuficientesException extends ProduccionException {

    private final Map<String, Integer> inventarioActual;

    public IngredientesInsuficientesException(String message, Map<String, Integer> inventarioActual) {
        super(message);
        this.inventarioActual = inventarioActual;
    }

    public Map<String, Integer> getInventarioActual() {
        return inventarioActual;
    }
}
//HOLA