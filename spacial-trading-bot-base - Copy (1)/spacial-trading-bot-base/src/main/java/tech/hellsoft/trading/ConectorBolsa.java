package tech.hellsoft.trading;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.hellsoft.trading.eventos.EventListener;
import tech.hellsoft.trading.dto.server.*;

/**
 * Stub del SDK oficial.
 * No contiene lógica, solo reenvía callbacks simulados.
 */
public class ConectorBolsa {

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(EventListener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    public void conectar(String host, String apiKey) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(apiKey, "apiKey");

        // En un SDK real se enviaría INIT + LOGIN
        // Aquí no hacemos nada
    }

    // ─────────────────────────────────────────
    // MÉTODOS DE SIMULACIÓN DE EVENTOS
    // ─────────────────────────────────────────

    public void simulateLogin(LoginOKMessage login) {
        listeners.forEach(l -> l.onLoginOk(login));
    }

    public void simulateTicker(TickerMessage ticker) {
        listeners.forEach(l -> l.onTicker(ticker));
    }

    public void simulateFill(FillMessage fill) {
        listeners.forEach(l -> l.onFill(fill));
    }

    public void simulateOffer(OfferMessage offer) {
        listeners.forEach(l -> l.onOffer(offer));
    }

    public void simulateOrderAck(OrderAckMessage ack) {
        listeners.forEach(l -> l.onOrderAck(ack));
    }

    public void simulateBalanceUpdate(BalanceUpdateMessage msg) {
        listeners.forEach(l -> l.onBalanceUpdate(msg));
    }

    public void simulateInventoryUpdate(InventoryUpdateMessage msg) {
        listeners.forEach(l -> l.onInventoryUpdate(msg));
    }

    public void simulateEventDelta(EventDeltaMessage msg) {
        listeners.forEach(l -> l.onEventDelta(msg));
    }

    public void simulateBroadcast(BroadcastNotificationMessage msg) {
        listeners.forEach(l -> l.onBroadcast(msg));
    }

    public void simulateDisconnect(Throwable cause) {
        listeners.forEach(l -> l.onConnectionLost(cause));
    }

    // ─────────────────────────────────────────
    // COMANDOS SIMULADOS
    // ─────────────────────────────────────────

    public void aceptarOferta(String id) {
        BroadcastNotificationMessage m = new BroadcastNotificationMessage();
        m.setMessage("Oferta " + id + " aceptada");
        simulateBroadcast(m);
    }

    public void rechazarOferta(String id) {
        BroadcastNotificationMessage m = new BroadcastNotificationMessage();
        m.setMessage("Oferta " + id + " rechazada");
        simulateBroadcast(m);
    }
}
