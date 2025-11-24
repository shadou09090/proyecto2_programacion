package tech.hellsoft.trading.exception.TradingExceptions;

import java.util.List;

public class ProductoNoAutorizadoException extends TradingException {
    public ProductoNoAutorizadoException(String message, List<String> productosAutorizados) {
        super(message);
    }
}
