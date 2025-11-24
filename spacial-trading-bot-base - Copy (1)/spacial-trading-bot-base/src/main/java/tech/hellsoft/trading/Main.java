package tech.hellsoft.trading;


import java.util.Map;
import java.util.Scanner;
import tech.hellsoft.trading.config.Configuration;
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
import tech.hellsoft.trading.modelo.Receta;
import tech.hellsoft.trading.modelo.Rol;
import tech.hellsoft.trading.util.CalculadoraProduccion;
import tech.hellsoft.trading.util.ConfigLoader;
import tech.hellsoft.trading.util.RecetaValidator;

/**
 * CLI Trading Bot with interactive menu.
 *
 * Students should implement the TODO methods below to complete the trading bot
 * functionality.
 */
public final class Main {

  private Main() {


  }

  private static boolean running = true;

  public static void main(String[] args) {
    try {
      // 1. Load configuration (apiKey, team, host)
      Configuration config = ConfigLoader.load("src/main/resources/config.json");
      printBanner();
      System.out.println("🚀 Starting Trading Bot for team: " + config.team());
      System.out.println();

      // 2. Create connector and event listener
      ConectorBolsa connector = new ConectorBolsa();
      MyTradingBot bot = new MyTradingBot();
      connector.addListener(bot);

      // 3. Connect to server
      System.out.println("🔌 Connecting to: " + config.host());
      connector.conectar(config.host(), config.apiKey());
      System.out.println("✅ Connected! Waiting for login...");
      System.out.println();

      // 4. Interactive CLI menu
      runInteractiveCLI(connector, bot);

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void printBanner() {
    System.out.println("╔══════════════════════════════════════════════════════════╗");
    System.out.println("║  🥑 Bolsa Interestelar de Aguacates Andorianos 🥑      ║");
    System.out.println("║  Trading Bot CLI - Java 25 Edition                      ║");
    System.out.println("╚══════════════════════════════════════════════════════════╝");
    System.out.println();
  }

  private static void runInteractiveCLI(ConectorBolsa connector, MyTradingBot bot) {
    Scanner scanner = new Scanner(System.in);

    while (running) {
      printMenu();
      System.out.print("\n> ");

      if (!scanner.hasNextLine()) {
        break;
      }

      String input = scanner.nextLine().trim();

      // Guard clause - skip empty input
      if (input.isEmpty()) {
        continue;
      }

      String[] parts = input.split("\\s+");
      String command = parts[0].toLowerCase();

      handleCommand(command, parts, connector, bot);
    }

    scanner.close();
    System.out.println("\n👋 Cerrando Trading Bot...");
    System.out.println("✅ ¡Hasta luego!");
  }

  private static void printMenu() {
    System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📋 COMANDOS DISPONIBLES:");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("  status              - Ver estado actual (saldo, P&L)");
    System.out.println("  inventario          - Ver productos en inventario");
    System.out.println("  precios             - Ver precios de mercado");
    System.out.println("  comprar <producto> <cantidad> [mensaje]");
    System.out.println("  vender <producto> <cantidad> [mensaje]");
    System.out.println("  producir <producto> <basico|premium>");
    System.out.println("  ofertas             - Ver ofertas pendientes");
    System.out.println("  aceptar <offerId>   - Aceptar una oferta");
    System.out.println("  ayuda               - Mostrar ayuda completa");
    System.out.println("  exit                - Salir del programa");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  }

  private static void handleCommand(String command, String[] parts, ConectorBolsa connector, MyTradingBot bot) {
    switch (command) {
    case "status" :
      handleStatus(bot);
      break;

    case "inventario" :
      handleInventario(bot);
      break;

    case "precios" :
      handlePrecios(bot);
      break;

    case "comprar" :
      handleComprar(parts, connector, bot);
      break;

    case "vender" :
      handleVender(parts, connector, bot);
      break;

    case "producir" :
      handleProducir(parts, connector, bot);
      break;

    case "ofertas" :
      handleOfertas(bot);
      break;

    case "aceptar" :
      handleAceptarOferta(parts, connector, bot);
      break;

    case "ayuda" :
    case "help" :
      printHelp();
      break;

    case "exit" :
    case "quit" :
    case "salir" :
      running = false;
      break;

    default :
      System.out.println("❌ Comando desconocido: " + command);
      System.out.println("💡 Escribe 'ayuda' para ver todos los comandos");
    }
  }

  private static void handleStatus(MyTradingBot bot) {
    double saldo = bot.getSaldo();
    double valorInventario = bot.calcularValorInventario();
    double patrimonioNeto = saldo + valorInventario;
    double pnl = bot.calcularPL();

    System.out.println("\n📊 ESTADO ACTUAL");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━");
    System.out.printf("💰 Saldo: $%.2f%n", saldo);
    System.out.printf("📦 Valor inventario: $%.2f%n", valorInventario);
    System.out.printf("💎 Patrimonio neto: $%.2f%n", patrimonioNeto);
    System.out.printf("📈 P&L: %+,.2f%%%n", pnl);
  }

  private static void handleInventario(MyTradingBot bot) {
    Map<String, Integer> inventario = bot.getInventario();

    System.out.println("\n📦 INVENTARIO");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    if (inventario.isEmpty()) {
      System.out.println("(vacío)");
      return;
    }

    inventario.forEach((producto, cantidad) -> {
      double precio = bot.getMidPrice(producto);
      double valor = cantidad * precio;
      System.out.printf("- %s: %d unidades | $%.2f c/u | Valor: $%.2f%n", producto, cantidad, precio, valor);
    });
  }

  private static void handlePrecios(MyTradingBot bot) {
    Map<String, MyTradingBot.PriceSnapshot> precios = bot.getPrecios();

    System.out.println("\n💹 PRECIOS DE MERCADO");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    if (precios.isEmpty()) {
      System.out.println("(esperando tickers...)");
      return;
    }

    precios.forEach((producto, snapshot) -> {
      System.out.printf("- %s | Bid: $%.2f | Ask: $%.2f | Mid: $%.2f%n",
          producto, snapshot.bestBid(), snapshot.bestAsk(), snapshot.mid());
    });
  }

  private static void handleComprar(String[] parts, ConectorBolsa connector, MyTradingBot bot) {
    if (parts.length < 3) {
      System.out.println("❌ Uso: comprar <producto> <cantidad> [mensaje]");
      return;
    }

    String producto = parts[1];
    int cantidad = Integer.parseInt(parts[2]);
    String mensaje = parts.length > 3
        ? String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length))
        : "Orden de compra";

    System.out.println("\n📤 Enviando orden de compra:");
    System.out.println("   Producto: " + producto);
    System.out.println("   Cantidad: " + cantidad);
    System.out.println("   Mensaje: " + mensaje);
    System.out.println();

    double precio = bot.getAskPrice(producto);
    if (precio <= 0) {
      System.out.println("⚠️ No hay precio disponible para " + producto);
      return;
    }

    double costo = precio * cantidad;
    if (bot.getSaldo() < costo) {
      System.out.printf("❌ Saldo insuficiente. Necesitas $%.2f y tienes $%.2f%n", costo, bot.getSaldo());
      return;
    }

    bot.debitarSaldo(costo);
    bot.agregarInventario(producto, cantidad);
    System.out.printf("✅ Orden de compra simulada: %d %s @ $%.2f (costo: $%.2f)%n", cantidad, producto, precio, costo);
  }

  private static void handleVender(String[] parts, ConectorBolsa connector, MyTradingBot bot) {
    if (parts.length < 3) {
      System.out.println("❌ Uso: vender <producto> <cantidad> [mensaje]");
      return;
    }

    String producto = parts[1];
    int cantidad = Integer.parseInt(parts[2]);
    String mensaje = parts.length > 3
        ? String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length))
        : "Orden de venta";

    System.out.println("\n📤 Enviando orden de venta:");
    System.out.println("   Producto: " + producto);
    System.out.println("   Cantidad: " + cantidad);
    System.out.println("   Mensaje: " + mensaje);
    System.out.println();

    int disponible = bot.getInventario().getOrDefault(producto, 0);
    if (disponible < cantidad) {
      System.out.printf("❌ Inventario insuficiente. Tienes %d unidades de %s%n", disponible, producto);
      return;
    }

    double precio = bot.getBidPrice(producto);
    if (precio <= 0) {
      System.out.println("⚠️ No hay precio disponible para " + producto);
      return;
    }

    double ingreso = precio * cantidad;
    bot.retirarInventario(producto, cantidad);
    bot.acreditarSaldo(ingreso);
    System.out.printf("✅ Orden de venta simulada: %d %s @ $%.2f (ingreso: $%.2f)%n", cantidad, producto, precio, ingreso);
  }

  private static void handleProducir(String[] parts, ConectorBolsa connector, MyTradingBot bot) {
    if (parts.length < 3) {
      System.out.println("❌ Uso: producir <producto> <basico|premium>");
      return;
    }

    String producto = parts[1];
    String tipo = parts[2].toLowerCase();
    boolean premium = tipo.equals("premium");

    System.out.println("\n🏭 Produciendo " + producto + " (" + tipo + "):");
    System.out.println();

    if (!bot.estaAutorizado(producto)) {
      System.out.println("❌ Producto no autorizado: " + producto);
      return;
    }

    int unidades = bot.producir(producto, premium);
    if (unidades == 0) {
      System.out.println("⚠️ No se produjo ninguna unidad.");
      return;
    }

    System.out.printf("✅ Producción simulada: %d unidades de %s%n", unidades, producto);
  }

  private static void handleOfertas(MyTradingBot bot) {
    System.out.println("\n📬 OFERTAS PENDIENTES");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    if (!bot.tieneOfertas()) {
      System.out.println("(sin ofertas pendientes)");
      return;
    }

    bot.getOfertas().forEach((id, oferta) -> {
      System.out.printf("- %s | %s x%d @ $%.2f%n",
          id, oferta.getProduct(), oferta.getQuantity(), oferta.getPrice());
    });
  }

  private static void handleAceptarOferta(String[] parts, ConectorBolsa connector, MyTradingBot bot) {
    if (parts.length < 2) {
      System.out.println("❌ Uso: aceptar <offerId>");
      return;
    }

    String offerId = parts[1];

    System.out.println("\n✅ Aceptando oferta: " + offerId);
    System.out.println();

    if (!bot.tieneOferta(offerId)) {
      System.out.println("❌ Oferta no encontrada: " + offerId);
      return;
    }

    OfferMessage oferta = bot.getOferta(offerId);
    int disponible = bot.getInventario().getOrDefault(oferta.getProduct(), 0);
    if (disponible < oferta.getQuantity()) {
      System.out.printf("❌ No tienes suficiente %s. Disponible: %d%n", oferta.getProduct(), disponible);
      return;
    }

    bot.retirarInventario(oferta.getProduct(), oferta.getQuantity());
    bot.acreditarSaldo(oferta.getPrice() * oferta.getQuantity());
    bot.removerOferta(offerId);
    System.out.println("✅ Oferta aceptada y saldo actualizado.");
  }

  private static void printHelp() {
    System.out.println("\n📚 AYUDA COMPLETA - Comandos del Trading Bot");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println();
    System.out.println("INFORMACIÓN:");
    System.out.println("  status              - Muestra saldo, inventario, P&L%");
    System.out.println("  inventario          - Lista todos tus productos");
    System.out.println("  precios             - Precios actuales de mercado");
    System.out.println();
    System.out.println("TRADING:");
    System.out.println("  comprar PALTA-OIL 10 \"mensaje opcional\"");
    System.out.println("  vender FOSFO 5 \"otro mensaje\"");
    System.out.println();
    System.out.println("PRODUCCIÓN:");
    System.out.println("  producir PALTA-OIL basico");
    System.out.println("  producir GUACA premium");
    System.out.println();
    System.out.println("OFERTAS:");
    System.out.println("  ofertas             - Ver ofertas de otros traders");
    System.out.println("  aceptar OFFER-123   - Aceptar una oferta específica");
    System.out.println();
    System.out.println("OTROS:");
    System.out.println("  ayuda               - Muestra esta ayuda");
    System.out.println("  exit                - Salir del programa");
    System.out.println();
    System.out.println("💡 TIP: Lee AGENTS.md para guía de implementación");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  }

  /**
   * Your trading bot implementation.
   *
   * TODO for students: - Add your trading logic in each callback method - Store
   * state (inventory, balance, prices, etc.) - Implement buy/sell strategies -
   * Handle production logic
   */
  private static class MyTradingBot implements EventListener {

    private final Map<String, PriceSnapshot> precios = new java.util.HashMap<>();
    private final Map<String, OfferMessage> ofertasPendientes = new java.util.LinkedHashMap<>();
    private final tech.hellsoft.trading.Cliente.EstadoCliente estado = new tech.hellsoft.trading.Cliente.EstadoCliente();
    private Rol rol = new Rol(10.0, 2, 0.8, 2.0, 3);

    MyTradingBot() {
      estado.setProductosAutorizados(java.util.List.of("PALTA-OIL", "GUACA", "FOSFO"));
    }

    @Override
    public void onLoginOk(LoginOKMessage loginOk) {
      // Guard clause
      if (loginOk == null) {
        return;
      }

      System.out.println("✅ LOGIN SUCCESSFUL!");
      System.out.println("   Team: " + loginOk.getTeam());
      System.out.println("   Species: " + loginOk.getSpecies());
      System.out.println("   Balance: $" + loginOk.getCurrentBalance());
      System.out.println();

      estado.setSaldo(loginOk.getCurrentBalance());
      estado.setSaldoInicial(loginOk.getCurrentBalance());
    }

    @Override
    public void onError(ErrorMessage error) {
      // Guard clause
      if (error == null) {
        return;
      }

      System.err.println("❌ ERROR [" + error.getCode() + "]: " + error.getReason());
    }

    @Override
    public void onTicker(TickerMessage ticker) {
      // Guard clause
      if (ticker == null) {
        return;
      }

      PriceSnapshot snapshot = new PriceSnapshot(ticker.getBestBid(), ticker.getBestAsk());
      precios.put(ticker.getProduct(), snapshot);
      estado.getPreciosActuales().put(ticker.getProduct(), snapshot.mid());

      System.out.println("📊 TICKER: " + ticker.getProduct() + " | Bid: $" + ticker.getBestBid() + " | Ask: $"
          + ticker.getBestAsk() + " | Mid: $" + ticker.getMid());
    }

    @Override
    public void onFill(FillMessage fill) {
      // Guard clause
      if (fill == null) {
        return;
      }

      System.out.println("✅ FILL: " + fill.getSide() + " " + fill.getFillQty() + " " + fill.getProduct() + " @ $"
          + fill.getFillPrice());

      boolean compra = "BUY".equalsIgnoreCase(fill.getSide());
      double valor = fill.getFillQty() * fill.getFillPrice();

      if (compra) {
        debitarSaldo(valor);
        agregarInventario(fill.getProduct(), fill.getFillQty());
        return;
      }

      retirarInventario(fill.getProduct(), fill.getFillQty());
      acreditarSaldo(valor);
    }

    @Override
    public void onBalanceUpdate(BalanceUpdateMessage balanceUpdate) {
      // Guard clause
      if (balanceUpdate == null) {
        return;
      }

      estado.setSaldo(balanceUpdate.getBalance());
      System.out.println("💰 BALANCE UPDATE: " + balanceUpdate);
    }

    @Override
    public void onInventoryUpdate(InventoryUpdateMessage inventoryUpdate) {
      // Guard clause
      if (inventoryUpdate == null) {
        return;
      }

      estado.getInventario().put(inventoryUpdate.getProduct(), inventoryUpdate.getQuantity());
      System.out.println("📦 INVENTORY UPDATE: " + inventoryUpdate);
    }

    @Override
    public void onOffer(OfferMessage offer) {
      if (offer == null) {
        return;
      }

      ofertasPendientes.put(offer.getOfferId(), offer);
      System.out.println("📨 Oferta recibida: " + offer);
    }

    @Override
    public void onOrderAck(OrderAckMessage orderAck) {
      if (orderAck == null) {
        return;
      }
      System.out.println("📬 Orden confirmada: " + orderAck);
    }

    @Override
    public void onEventDelta(EventDeltaMessage eventDelta) {
      if (eventDelta == null) {
        return;
      }
      System.out.println("📈 Evento recibido: " + eventDelta);
    }

    @Override
    public void onBroadcast(BroadcastNotificationMessage broadcast) {
      // Guard clause
      if (broadcast == null) {
        return;
      }

      System.out.println("📢 BROADCAST: " + broadcast.getMessage());
    }

    @Override
    public void onConnectionLost(Throwable throwable) {
      System.err.println("💔 CONNECTION LOST: " + throwable.getMessage());

      // TODO: Implement reconnection logic
    }

    double getSaldo() {
      return estado.getSaldo();
    }

    double calcularValorInventario() {
      return estado.getInventario().entrySet().stream()
          .mapToDouble(entry -> entry.getValue() * getMidPrice(entry.getKey()))
          .sum();
    }

    double calcularPL() {
      if (estado.getSaldoInicial() == 0) {
        return 0.0;
      }
      return estado.calcularPL();
    }

    Map<String, Integer> getInventario() {
      return java.util.Collections.unmodifiableMap(estado.getInventario());
    }

    Map<String, PriceSnapshot> getPrecios() {
      return java.util.Collections.unmodifiableMap(precios);
    }

    double getMidPrice(String producto) {
      PriceSnapshot snapshot = precios.get(producto);
      if (snapshot == null) {
        return 0.0;
      }
      return snapshot.mid();
    }

    double getBidPrice(String producto) {
      PriceSnapshot snapshot = precios.get(producto);
      if (snapshot == null) {
        return 0.0;
      }
      return snapshot.bestBid();
    }

    double getAskPrice(String producto) {
      PriceSnapshot snapshot = precios.get(producto);
      if (snapshot == null) {
        return 0.0;
      }
      return snapshot.bestAsk();
    }

    void debitarSaldo(double monto) {
      estado.setSaldo(estado.getSaldo() - monto);
    }

    void acreditarSaldo(double monto) {
      estado.setSaldo(estado.getSaldo() + monto);
    }

    void agregarInventario(String producto, int cantidad) {
      estado.getInventario().merge(producto, cantidad, Integer::sum);
    }

    void retirarInventario(String producto, int cantidad) {
      estado.getInventario().merge(producto, -cantidad, Integer::sum);
      estado.getInventario().remove(producto, 0);
    }

    boolean estaAutorizado(String producto) {
      return estado.getProductosAutorizados().contains(producto);
    }

    int producir(String producto, boolean premium) {
      Receta receta = estado.getRecetas().get(producto);
      if (premium && receta == null) {
        System.out.println("❌ No hay receta cargada para producción premium de " + producto);
        return 0;
      }

      if (premium && !RecetaValidator.puedeProducir(receta, estado.getInventario())) {
        System.out.println("❌ Ingredientes insuficientes para producción premium");
        return 0;
      }

      int unidadesBase = CalculadoraProduccion.calcularUnidades(rol);
      int unidades = premium
          ? CalculadoraProduccion.aplicarBonusPremium(unidadesBase, 1.3)
          : unidadesBase;

      if (premium) {
        receta.ingredientes().forEach((ingrediente, cantidad) ->
            retirarInventario(ingrediente, cantidad));
      }

      agregarInventario(producto, unidades);
      return unidades;
    }

    boolean tieneOfertas() {
      return !ofertasPendientes.isEmpty();
    }

    Map<String, OfferMessage> getOfertas() {
      return java.util.Collections.unmodifiableMap(ofertasPendientes);
    }

    boolean tieneOferta(String id) {
      return ofertasPendientes.containsKey(id);
    }

    OfferMessage getOferta(String id) {
      return ofertasPendientes.get(id);
    }

    void removerOferta(String id) {
      ofertasPendientes.remove(id);
    }
  }

  record PriceSnapshot(double bestBid, double bestAsk) {
    double mid() {
      return (bestBid + bestAsk) / 2.0;
    }
  }
}
