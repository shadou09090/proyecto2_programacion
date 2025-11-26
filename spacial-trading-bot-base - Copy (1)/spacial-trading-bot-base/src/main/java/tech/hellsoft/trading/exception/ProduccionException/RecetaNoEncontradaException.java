package tech.hellsoft.trading.exception.ProduccionException;

public class RecetaNoEncontradaException extends ProduccionException {

    private final String producto;

    public RecetaNoEncontradaException(String producto) {
        super("No se encontró la receta para el producto: " + producto);
        this.producto = producto;
    }

    public String getProducto() {
        return producto;
    }
}
