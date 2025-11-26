package tech.hellsoft.trading;

import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import tech.hellsoft.trading.Cliente.ClienteBolsa;
import tech.hellsoft.trading.Cliente.EstadoCliente;
import tech.hellsoft.trading.config.Configuration;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.dto.server.TickerMessage;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;
import tech.hellsoft.trading.util.ConfigLoader;

/**
 * CLI Trading Bot with interactive menu.
 */
public final class Main {

    private Main() {
    }

    private static boolean running = true;

    public static void main(String[] args) {
        try {
            Configuration config = ConfigLoader.load("src/main/resources/config.json");
            printBanner();
            System.out.println("🚀 Starting Trading Bot for team: " + config.team());
            System.out.println();

            // -------------------------------------------------------
            // Crear EstadoCliente compartido y pasar al ClienteBolsa
            // para que consola y conector trabajen sobre la misma instancia.
            // -------------------------------------------------------
            EstadoCliente sharedEstado = new EstadoCliente();

            ConectorBolsa connector = new ConectorBolsa();
            ClienteBolsa cliente = new ClienteBolsa(connector, sharedEstado);

            // registrar el cliente como listener en el conector
            connector.addListener(cliente);

            System.out.println("🔌 Connecting to: " + config.host());
            connector.conectar(config.host(), config.apiKey());

            // Hacemos login; si hay problemas de firmas, pasar null está bien porque ya añadimos el listener.
            // Si tu ConectorBolsa requiere el listener en login y acepta cliente, puedes cambiar a:
            // connector.login(config.apiKey(), cliente);
            connector.login(config.apiKey(), null);

            System.out.println("✅ Conectado. Esperando eventos de login...");
            System.out.println();



            runInteractiveCLI(cliente);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  🥑 Bolsa Interestelar de Aguacates Andorianos 🥑              ║");
        System.out.println("║  Trading Bot CLI - Java 25 Edition                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void runInteractiveCLI(ClienteBolsa cliente) {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            printMenu();
            System.out.print("\n> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            handleCommand(command, parts, cliente);
        }

        scanner.close();
        System.out.println("\n👋 Cerrando Trading Bot...");
        System.out.println("✅ ¡Hasta luego!");
    }

    private static void printMenu() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📋 COMANDOS DISPONIBLES:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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

    private static void handleCommand(String command, String[] parts, ClienteBolsa cliente) {
        switch (command) {
            case "status" :
                handleStatus(cliente);
                break;

            case "inventario" :
                handleInventario(cliente);
                break;

            case "precios" :
                handlePrecios(cliente);
                break;

            case "comprar" :
                handleComprar(parts, cliente);
                break;

            case "vender" :
                handleVender(parts, cliente);
                break;

            case "producir" :
                handleProducir(parts, cliente);
                break;

            case "ofertas" :
                handleOfertas(cliente);
                break;

            case "aceptar" :
                handleAceptarOferta(parts, cliente);
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

    private static void handleStatus(ClienteBolsa cliente) {
        EstadoCliente estado = cliente.getEstado();
        double saldo = estado.getSaldo();
        double valorInventario = estado.calcularValorInventario();
        double patrimonio = estado.calcularPatrimonioNeto();
        double pl = estado.calcularPLPorcentaje();

        System.out.println("\n📊 ESTADO ACTUAL");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💰 Saldo: $" + formatCurrency(saldo));
        System.out.println("📦 Valor inventario: $" + formatCurrency(valorInventario));
        System.out.println("💎 Patrimonio neto: $" + formatCurrency(patrimonio));
        System.out.println("📈 P&L: " + formatPercentage(pl) + "%");
    }

    private static void handleInventario(ClienteBolsa cliente) {
        EstadoCliente estado = cliente.getEstado();
        Map<String, Integer> inventario = estado.getInventario();

        System.out.println("\n📦 INVENTARIO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (inventario.isEmpty()) {
            System.out.println("(sin productos)");
            return;
        }

        inventario.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String producto = entry.getKey();
                    int cantidad = entry.getValue();
                    double precioMid = estado.getPreciosActuales().getOrDefault(producto, 0.0);
                    double valor = precioMid * cantidad;
                    System.out.println("- " + producto + ": " + cantidad + " uds @ $" + formatCurrency(precioMid)
                            + " => $" + formatCurrency(valor));
                });
    }

    private static void handlePrecios(ClienteBolsa cliente) {
        EstadoCliente estado = cliente.getEstado();
        Map<String, TickerMessage> tickers = estado.getUltimosTickers();

        System.out.println("\n💹 PRECIOS DE MERCADO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (tickers.isEmpty()) {
            System.out.println("(sin tickers recibidos aún)");
            return;
        }

        tickers.values().stream()
                .sorted(Comparator.comparing(TickerMessage::getProduct))
                .forEach(ticker -> {
                    System.out.println("- " + ticker.getProduct() + " | Bid: $" + formatCurrency(ticker.getBestBid())
                            + " | Ask: $" + formatCurrency(ticker.getBestAsk())
                            + " | Mid: $" + formatCurrency(ticker.getMid()));
                });
    }

    private static void handleComprar(String[] parts, ClienteBolsa cliente) {
        if (parts.length < 3) {
            System.out.println("❌ Uso: comprar <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = parts[1];
        int cantidad;
        try {
            cantidad = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            System.out.println("❌ La cantidad debe ser numérica");
            return;
        }

        String mensaje = parts.length > 3
                ? String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length))
                : "Orden de compra";

        System.out.println("\n📤 Enviando orden de compra:");
        System.out.println("   Producto: " + producto);
        System.out.println("   Cantidad: " + cantidad);
        System.out.println("   Mensaje: " + mensaje);

        try {
            cliente.comprar(producto, cantidad, mensaje);
        } catch (SaldoInsuficienteException e) {
            System.out.println("❌ Saldo insuficiente: tienes $" + formatCurrency(e.getSaldoDisponible())
                    + " y necesitas $" + formatCurrency(e.getSaldoRequerido()));
        } catch (RuntimeException e) {
            System.out.println("❌ No se pudo enviar la orden: " + e.getMessage());
        }
    }

    private static void handleVender(String[] parts, ClienteBolsa cliente) {
        if (parts.length < 3) {
            System.out.println("❌ Uso: vender <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = parts[1];
        int cantidad;
        try {
            cantidad = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            System.out.println("❌ La cantidad debe ser numérica");
            return;
        }

        String mensaje = parts.length > 3
                ? String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length))
                : "Orden de venta";

        System.out.println("\n📤 Enviando orden de venta:");
        System.out.println("   Producto: " + producto);
        System.out.println("   Cantidad: " + cantidad);
        System.out.println("   Mensaje: " + mensaje);

        try {
            cliente.vender(producto, cantidad, mensaje);
        } catch (InventarioInsuficienteException e) {
            System.out.println("❌ Inventario insuficiente para " + e.getProducto()
                    + ". Tienes " + e.getDisponible() + " y solicitaste " + e.getSolicitado());
        }
    }

    private static void handleProducir(String[] parts, ClienteBolsa cliente) {
        if (parts.length < 3) {
            System.out.println("❌ Uso: producir <producto> <basico|premium>");
            return;
        }

        String producto = parts[1];
        String tipo = parts[2].toLowerCase();
        boolean premium = tipo.equals("premium");

        System.out.println("\n🏭 Produciendo " + producto + " (" + tipo + "):");

        try {
            cliente.producir(producto, premium);
        } catch (ProductoNoAutorizadoException e) {
            System.out.println("❌ Producto no autorizado: " );
        } catch (RecetaNoEncontradaException e) {
            System.out.println("❌ No se encontró la receta para " );
        } catch (IngredientesInsuficientesException e) {
            System.out.println("❌ Ingredientes insuficientes para producción premium");
        }
    }

    private static void handleOfertas(ClienteBolsa cliente) {
        Map<String, OfferMessage> ofertas = cliente.getOfertas();

        System.out.println("\n📬 OFERTAS PENDIENTES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (ofertas.isEmpty()) {
            System.out.println("(sin ofertas pendientes)");
            return;
        }

        ofertas.values().stream()
                .sorted(Comparator.comparing(OfferMessage::getOfferId))
                .forEach(offer -> {
                    System.out.println("- " + offer.getOfferId() + " | " + offer.getProduct() + " x"
                            + offer.getQuantity() + " @ $" + formatCurrency(offer.getPrice()));
                });
    }

    private static void handleAceptarOferta(String[] parts, ClienteBolsa cliente) {
        if (parts.length < 2) {
            System.out.println("❌ Uso: aceptar <offerId>");
            return;
        }

        String offerId = parts[1];

        if (!cliente.tieneOferta(offerId)) {
            System.out.println("❌ No existe la oferta " + offerId);
            return;
        }

        cliente.aceptarOferta(offerId);
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

    private static String formatCurrency(double value) {
        return String.format("%.2f", value);
    }

    private static String formatPercentage(double value) {
        return String.format("%.2f", value);
    }
}