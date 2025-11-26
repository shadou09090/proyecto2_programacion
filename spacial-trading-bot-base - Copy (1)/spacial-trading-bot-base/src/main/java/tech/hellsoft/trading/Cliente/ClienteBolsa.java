package tech.hellsoft.trading.Cliente;

import java.util.HashMap;
import java.util.Map;
import tech.hellsoft.trading.ConectorBolsa;
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
import tech.hellsoft.trading.eventos.EventListener;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;

public class ClienteBolsa implements EventListener {

  private final ConectorBolsa conector;
  private final EstadoCliente estado;
  private final Map<String, OfferMessage> ofertas = new HashMap<>();

  public ClienteBolsa(ConectorBolsa conector, EstadoCliente sharedEstado) {
    this.conector = conector;
    this.estado = new EstadoCliente();
  }

  public void restaurarEstado(EstadoCliente nuevo) {
    this.estado.copiarDesde(nuevo);
  }

  // ========== CALLBACKS DEL SDK ==========
  @Override
  public void onLoginOk(LoginOKMessage msg) {
    if (msg == null) {
      return;
    }
    estado.setSaldo(msg.getCurrentBalance());
    estado.setSaldoInicial(msg.getCurrentBalance());
    System.out.println("✅ Conectado como " + msg.getTeam());
  }

  @Override
  public void onFill(FillMessage fill) {
    if (fill == null) {
      return;
    }
    boolean isBuy = "BUY".equalsIgnoreCase(fill.getSide());
    if (isBuy) {
      estado.getInventario().merge(fill.getProduct(), fill.getFillQty(), Integer::sum);
      double nuevoSaldo = estado.getSaldo() - fill.getFillQty() * fill.getFillPrice();
      estado.setSaldo(nuevoSaldo);
    }
    if (!isBuy) {
      estado.getInventario().merge(fill.getProduct(), -fill.getFillQty(), Integer::sum);
      double nuevoSaldo = estado.getSaldo() + fill.getFillQty() * fill.getFillPrice();
      estado.setSaldo(nuevoSaldo);
    }
    System.out.println("P&L: " + estado.calcularPLPorcentaje() + "%");
  }

    @Override
    public void onTicker(TickerMessage ticker) {
        if (ticker == null) return;

        System.out.println("[DEBUG onTicker] cliente=" + this
                + " estado.hash=" + System.identityHashCode(estado)
                + " product=" + ticker.getProduct() + " mid=" + ticker.getMid());

        estado.getPreciosActuales().put(ticker.getProduct(), ticker.getMid());
        estado.getUltimosTickers().put(ticker.getProduct(), ticker);
    }

  @Override
  public void onOffer(OfferMessage offer) {
    if (offer == null) {
      return;
    }
    ofertas.put(offer.getOfferId(), offer);
    System.out.println("📨 Oferta recibida: " + offer.getOfferId());
  }

  @Override
  public void onError(ErrorMessage error) {
    if (error == null) {
      return;
    }
    if ("INVALID_TOKEN".equals(error.getCode())) {
      System.exit(1);
      return;
    }
    System.err.println("❌ Error: " + error.getReason());
  }

  @Override
  public void onConnectionLost(Throwable throwable) {
    String mensaje = throwable == null ? "Desconocido" : throwable.getMessage();
    System.out.println("⚠ Conexión perdida: " + mensaje);
  }

  @Override
  public void onBalanceUpdate(BalanceUpdateMessage balanceUpdate) {
    if (balanceUpdate == null) {
      return;
    }
    double balance = balanceUpdate.getBalance();
    estado.setSaldo(balance);
    if (estado.getSaldoInicial() == 0) {
      estado.setSaldoInicial(balance);
    }
  }

    @Override
    public void onInventoryUpdate(InventoryUpdateMessage inventoryUpdate) {
        if (inventoryUpdate == null) {
            return;
        }

        // DEBUG: mostrar que llegó el evento y a qué instancia de EstadoCliente afecta
        System.out.println("[DEBUG onInventoryUpdate] cliente=" + this
                + " estado.hash=" + System.identityHashCode(estado)
                + " product=" + inventoryUpdate.getProduct()
                + " qty=" + inventoryUpdate.getQuantity());

        // actualizar estado
        estado.getInventario().put(inventoryUpdate.getProduct(), inventoryUpdate.getQuantity());

        // DEBUG: mostrar inventario resultante
        System.out.println("[DEBUG afterPut] estado.inventario=" + estado.getInventario());
    }

  @Override
  public void onOrderAck(OrderAckMessage orderAck) {
  }

  @Override
  public void onEventDelta(EventDeltaMessage eventDelta) {
  }

  @Override
  public void onBroadcast(BroadcastNotificationMessage broadcast) {
  }

  // ========== MÉTODOS PÚBLICOS ==========
  public void comprar(String producto, int cantidad, String mensaje)
          throws SaldoInsuficienteException {

    Double precio = estado.getPreciosActuales().get(producto);
    if (precio == null) {
      throw new RuntimeException("No hay precio actual para " + producto);
    }

    double costo = precio * cantidad * 1.05; // margen 5%

    if (estado.getSaldo() < costo) {
      throw new SaldoInsuficienteException(estado.getSaldo(), costo);
    }

    System.out.println("Orden enviada al servidor (simulada): BUY "
            + cantidad + " " + producto + " | mensaje=\"" + mensaje + "\"");
  }

  public void vender(String producto, int cantidad, String mensaje)
          throws InventarioInsuficienteException {

    int inv = estado.getInventario().getOrDefault(producto, 0);

    if (cantidad > inv) {
      throw new InventarioInsuficienteException(producto, inv, cantidad);
    }

    System.out.println("Orden enviada al servidor (simulada): SELL "
            + cantidad + " " + producto + " | mensaje=\"" + mensaje + "\"");
  }

  public void producir(String producto, boolean premium)
          throws ProductoNoAutorizadoException,
          RecetaNoEncontradaException,
          IngredientesInsuficientesException {

    if (!estado.getProductosAutorizados().contains(producto)) {
      throw new ProductoNoAutorizadoException(producto, estado.getProductosAutorizados());
    }

    var receta = estado.getRecetas().get(producto);
    if (receta == null) {
      throw new RecetaNoEncontradaException(producto);
    }

    if (premium) {
      throw new IngredientesInsuficientesException(null, estado.getInventario());
    }

    int unidades = 1;
    estado.getInventario().merge(producto, unidades, Integer::sum);

    System.out.println("Producción enviada (simulada): "
            + unidades + " de " + producto + (premium ? " (premium)" : ""));
  }

  // ========== OFERTAS ==========
  public Map<String, OfferMessage> getOfertas() {
    return ofertas;
  }

  public boolean tieneOferta(String id) {
    return ofertas.containsKey(id);
  }

  public OfferMessage getOferta(String id) {
    return ofertas.get(id);
  }

  public void aceptarOferta(String id) {
    if (!ofertas.containsKey(id)) {
      return;
    }

    conector.aceptarOferta(id);
    System.out.println("Oferta aceptada: " + id);
    ofertas.remove(id);
  }

  public void rechazarOferta(String id) {
    if (!ofertas.containsKey(id)) {
      return;
    }

    conector.rechazarOferta(id);
    System.out.println("Oferta rechazada: " + id);
    ofertas.remove(id);
  }

  public EstadoCliente getEstado() {
    return estado;
  }
}
