package tech.hellsoft.trading.Cliente;

import tech.hellsoft.trading.ConectorBolsa;
import tech.hellsoft.trading.dto.server.ErrorMessage;
import tech.hellsoft.trading.dto.server.FillMessage;
import tech.hellsoft.trading.dto.server.LoginOKMessage;
import tech.hellsoft.trading.dto.server.OfferMessage;
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

  public ClienteBolsa(ConectorBolsa conector) {
    this.conector = conector;
    this.estado = new EstadoCliente();
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
    }
    if (!isBuy) {
      estado.getInventario().merge(fill.getProduct(), -fill.getFillQty(), Integer::sum);
    }
    System.out.println("P&L: " + estado.calcularPL() + "%");
  }

  @Override
  public void onTicker(TickerMessage ticker) {
    if (ticker == null) {
      return;
    }
    estado.getPreciosActuales().put(ticker.getProduct(), ticker.getMid());
  }

  @Override
  public void onOffer(OfferMessage offer) {
    if (offer == null) {
      return;
    }
    System.out.println("📨 Oferta recibida: " + offer);
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

  // Métodos no utilizados en este stub
  @Override
  public void onBalanceUpdate(tech.hellsoft.trading.dto.server.BalanceUpdateMessage balanceUpdate) {
  }

  @Override
  public void onInventoryUpdate(tech.hellsoft.trading.dto.server.InventoryUpdateMessage inventoryUpdate) {
  }

  @Override
  public void onOrderAck(tech.hellsoft.trading.dto.server.OrderAckMessage orderAck) {
  }

  @Override
  public void onEventDelta(tech.hellsoft.trading.dto.server.EventDeltaMessage eventDelta) {
  }

  @Override
  public void onBroadcast(tech.hellsoft.trading.dto.server.BroadcastNotificationMessage broadcast) {
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
