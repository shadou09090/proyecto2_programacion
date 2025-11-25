package tech.hellsoft.trading.exception.ProduccionException;

import java.util.Map;

public class IngredientesInsuficientesException extends ProduccionException{

    public IngredientesInsuficientesException(String message, Map<String, Integer> inventario) {
        super(message);
    }

}
