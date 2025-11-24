package tech.hellsoft.trading.exception.TradingExceptions;

public class InventarioInsuficienteException extends TradingException {
    public InventarioInsuficienteException(String message) {
        super(message);
    }

    public InventarioInsuficienteException(String producto, int inv, int cantidad) {
        super("F");
    }
}
