package tech.hellsoft.trading.exception.TradingExceptions;

public class PrecioNoDisponibleException extends TradingException {

    private final String producto;

    public PrecioNoDisponibleException(String producto) {
        super("No hay precio disponible para el producto: " + producto);
        this.producto = producto;
    }

    public String getProducto() {
        return producto;
    }
}
