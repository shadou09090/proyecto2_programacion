package tech.hellsoft.trading;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import tech.hellsoft.trading.eventos.EventListener;
import tech.hellsoft.trading.dto.server.BalanceUpdateMessage;
import tech.hellsoft.trading.dto.server.BroadcastNotificationMessage;
import tech.hellsoft.trading.dto.server.ErrorMessage;
import tech.hellsoft.trading.dto.server.EventDeltaMessage;
import tech.hellsoft.trading.dto.server.FillMessage;
import tech.hellsoft.trading.dto.server.InventoryUpdateMessage;
import tech.hellsoft.trading.dto.server.LoginOKMessage;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.dto.server.OrderAckMessage;
import tech.hellsoft.trading.dto.server.TickerMessage;

/**
 * Stub connector that simulates the SDK client.
 *
 * The class provides minimal no-op implementations so the sample code can compile
 * without the real dependency.
 */
public class ConectorBolsa {

  private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

  public void addListener(EventListener listener) {
    if (listener == null) {
      return;
    }
    listeners.addIfAbsent(listener);
  }

  public void conectar(String host, String apiKey) {
    Objects.requireNonNull(host, "host");
    Objects.requireNonNull(apiKey, "apiKey");
  }

  // ===== Simulation helpers =====
  public void simulateLogin(LoginOKMessage login) {
    listeners.forEach(listener -> listener.onLoginOk(login));
  }

  public void simulateTicker(TickerMessage ticker) {
    listeners.forEach(listener -> listener.onTicker(ticker));
  }

  public void simulateFill(FillMessage fill) {
    listeners.forEach(listener -> listener.onFill(fill));
  }

  public void simulateOffer(OfferMessage offer) {
    listeners.forEach(listener -> listener.onOffer(offer));
  }

  public void simulateOrderAck(OrderAckMessage orderAck) {
    listeners.forEach(listener -> listener.onOrderAck(orderAck));
  }

  public void simulateBalanceUpdate(BalanceUpdateMessage balanceUpdate) {
    listeners.forEach(listener -> listener.onBalanceUpdate(balanceUpdate));
  }

  public void simulateInventoryUpdate(InventoryUpdateMessage inventoryUpdate) {
    listeners.forEach(listener -> listener.onInventoryUpdate(inventoryUpdate));
  }

  public void simulateEventDelta(EventDeltaMessage eventDelta) {
    listeners.forEach(listener -> listener.onEventDelta(eventDelta));
  }

  public void simulateBroadcast(BroadcastNotificationMessage broadcast) {
    listeners.forEach(listener -> listener.onBroadcast(broadcast));
  }

  public void simulateDisconnect(Throwable throwable) {
    listeners.forEach(listener -> listener.onConnectionLost(throwable));
  }
}
