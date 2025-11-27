package tech.hellsoft.trading.exception.TradingExceptions;

import java.util.Set;

public class ProductoNoAutorizadoException extends TradingException {

    private final String producto;
    private final Set<String> permitidos;

    public ProductoNoAutorizadoException(String producto, Set<String> permitidos) {
        super("Producto no autorizado: " + producto + ". Permitidos: " + permitidos);
        this.producto = producto;
        this.permitidos = permitidos;
    }

    public String getProducto() { return producto; }
    public Set<String> getPermitidos() { return permitidos; }
}
//HOLA