package tech.hellsoft.trading.Cliente;

import tech.hellsoft.trading.ConectorBolsa;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import tech.hellsoft.trading.ConectorBolsa;
import tech.hellsoft.trading.EventListener;
import tech.hellsoft.trading.dto.server.*;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;


public class ClienteBolsa implements EventListener {
    private ConectorBolsa conector;
    private EstadoCliente estado;
    public ClienteBolsa(ConectorBolsa conector) {
        this.conector = conector;
        this.estado = new EstadoCliente();
    }
    // ========== CALLBACKS DEL SDK ==========
    @Override
    public void onLoginOk(LoginOk msg) {
        // Inicializar estado con datos del servidor
        estado.setSaldo(msg.getSaldoInicial());
        estado.setSaldoInicial(msg.getSaldoInicial());
        estado.setRecetas(msg.getRecetas());
        estado.setRol(msg.getRol());
        estado.setProductosAutorizados(msg.getProductosAutorizados());
        System.out.println("✅ Conectado como " + msg.getEquipo());
    }
    @Override
    public void onFill(Fill fill) {
        if (fill.getSide().equals("BUY")) {
            // Restar dinero, sumar inventario
        } else {
            // Sumar dinero, restar inventario
        }
        System.out.println("P&L: " + estado.calcularPL() + "%");
    }
    @Override
    public void onTicker(Ticker ticker) {
        estado.getPreciosActuales().put(ticker.getProducto(), ticker.getMid());
    }
    @Override
    public void onOffer(Offer offer) {
        // Decidir si aceptar basado en precio y disponibilidad
    }

    @Override
    public void onLoginOk(LoginOKMessage message) {

    }

    @Override
    public void onFill(FillMessage message) {

    }

    @Override
    public void onTicker(TickerMessage message) {

    }

    @Override
    public void onOffer(OfferMessage message) {

    }

    @Override
    public void onError(ErrorMessage error) {
        switch (error.getCodigo()) {
            case "INVALID_TOKEN":
                System.exit(1);
                break;
            // ... más casos
        }
    }

    @Override
    public void onOrderAck(OrderAckMessage message) {

    }

    @Override
    public void onInventoryUpdate(InventoryUpdateMessage message) {

    }

    @Override
    public void onBalanceUpdate(BalanceUpdateMessage message) {

    }

    @Override
    public void onEventDelta(EventDeltaMessage message) {

    }

    @Override
    public void onBroadcast(BroadcastNotificationMessage message) {

    }

    @Override
    public void onConnectionLost(Throwable error) {

    }

    @Override
    public void onGlobalPerformanceReport(GlobalPerformanceReportMessage message) {

    }

    @Override
    public void onConexionPerdida(Exception e) {
        System.out.println("⚠ Conexión perdida");
    }
    // ========== MÉTODOS PÚBLICOS ==========
    public void comprar(String producto, int cantidad, String mensaje)
            throws SaldoInsuficienteException {
        // Validar saldo → lanzar excepción si falla
        // Crear orden → enviar
    }
    public void vender(String producto, int cantidad, String mensaje)
            throws InventarioInsuficienteException {
        // Validar inventario → lanzar excepción si falla
        // Crear orden → enviar
    }
    public void producir(String producto, boolean premium)
            throws ProductoNoAutorizadoException, RecetaNoEncontradaException,
            IngredientesInsuficientesException {
        // Validaciones → calcular → actualizar → notificar
    }
    public EstadoCliente getEstado() {
        return estado;
    }
}