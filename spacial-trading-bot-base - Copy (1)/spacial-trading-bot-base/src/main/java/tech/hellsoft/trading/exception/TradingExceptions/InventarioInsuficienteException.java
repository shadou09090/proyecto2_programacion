package tech.hellsoft.trading.exception.TradingExceptions;

public class InventarioInsuficienteException extends TradingException {

    private final String producto;
    private final int disponible;
    private final int solicitado;

    public InventarioInsuficienteException(String producto, int disponible, int solicitado) {
        super("Inventario insuficiente para " + producto +
                ". Disponible: " + disponible + ", solicitado: " + solicitado);
        this.producto = producto;
        this.disponible = disponible;
        this.solicitado = solicitado;
    }

    public String getProducto() { return producto; }
    public int getDisponible() { return disponible; }
    public int getSolicitado() { return solicitado; }
}
//HOLA