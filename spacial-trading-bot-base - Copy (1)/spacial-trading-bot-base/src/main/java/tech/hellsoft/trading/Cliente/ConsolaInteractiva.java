package tech.hellsoft.trading.Cliente;

import java.util.*;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;

public class ConsolaInteractiva {

    private final ClienteBolsa cliente;
    private final Scanner scanner = new Scanner(System.in);

    public ConsolaInteractiva(ClienteBolsa cliente) {
        this.cliente = cliente;
    }

    public void iniciar() {
        System.out.println("=== Bolsa Interestelar de Aguacates Andorianos ===");
        System.out.println("Escribe 'ayuda' para ver los comandos.");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;

            String linea = scanner.nextLine().trim();
            if (linea.isEmpty()) continue;

            String[] tokens = linea.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            try {
                switch (cmd) {
                    case "login":
                        handleLogin();
                        break;

                    case "status":
                        handleStatus();
                        break;

                    case "inventario":
                        handleInventario();
                        break;

                    case "precios":
                        handlePrecios();
                        break;

                    case "comprar":
                        handleComprar(tokens, linea);
                        break;

                    case "vender":
                        handleVender(tokens, linea);
                        break;

                    case "producir":
                        handleProducir(tokens);
                        break;

                    case "ayuda":
                    case "help":
                        imprimirAyuda();
                        break;

                    case "exit":
                        System.out.println("👋 Cerrando cliente...");
                        return;

                    default:
                        System.out.println("❓ Comando desconocido. Usa 'ayuda'.");
                }
            } catch (SaldoInsuficienteException |
                     InventarioInsuficienteException |
                     ProductoNoAutorizadoException |
                     IngredientesInsuficientesException |
                     RecetaNoEncontradaException e) {

                System.out.println("❌ " + e.getMessage());

            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────
    // COMANDOS
    // ─────────────────────────────

    private void handleLogin() {
        // El login real lo hace ConectorBolsa → ClienteBolsa recibe callback
        System.out.println("🔐 Esperando LOGIN_OK del servidor...");
        System.out.println("Si tu conector está corriendo, el callback onLoginOk se ejecutará.");
    }

    private void handleStatus() {
        EstadoCliente est = cliente.getEstado();
        double saldo = est.getSaldo();
        double pl = est.calcularPL();

        double valorInv = 0;
        for (var e : est.getInventario().entrySet()) {
            double precio = est.getPreciosActuales().getOrDefault(e.getKey(), 0.0);
            valorInv += e.getValue() * precio;
        }

        System.out.println("=== STATUS ===");
        System.out.printf("Saldo actual: %.2f%n", saldo);
        System.out.printf("Valor inventario: %.2f%n", valorInv);
        System.out.printf("Patrimonio total: %.2f%n", (saldo + valorInv));
        System.out.printf("P&L: %.2f%%%n", pl);
    }

    private void handleInventario() {
        EstadoCliente est = cliente.getEstado();

        System.out.println("=== INVENTARIO ===");
        if (est.getInventario().isEmpty()) {
            System.out.println("(vacío)");
            return;
        }

        for (var e : est.getInventario().entrySet()) {
            double precio = est.getPreciosActuales().getOrDefault(e.getKey(), 0.0);
            double valor = precio * e.getValue();
            System.out.printf("%s | qty=%d | precio=%.2f | valor=%.2f%n",
                    e.getKey(), e.getValue(), precio, valor);
        }
    }

    private void handlePrecios() {
        EstadoCliente est = cliente.getEstado();

        System.out.println("=== PRECIOS (MID) ===");
        if (est.getPreciosActuales().isEmpty()) {
            System.out.println("Aún no se han recibido precios.");
            return;
        }

        est.getPreciosActuales().forEach((prod, precio) -> {
            System.out.printf("%s: %.2f%n", prod, precio);
        });
    }

    private void handleComprar(String[] tokens, String linea)
            throws SaldoInsuficienteException {

        if (tokens.length < 3) {
            System.out.println("Uso: comprar <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = tokens[1];
        int cantidad = Integer.parseInt(tokens[2]);
        String mensaje = extraerMensaje(tokens, linea, 3);

        cliente.comprar(producto, cantidad, mensaje);
        System.out.println("✔ Orden de compra enviada.");
    }

    private void handleVender(String[] tokens, String linea)
            throws InventarioInsuficienteException {

        if (tokens.length < 3) {
            System.out.println("Uso: vender <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = tokens[1];
        int cantidad = Integer.parseInt(tokens[2]);
        String mensaje = extraerMensaje(tokens, linea, 3);

        cliente.vender(producto, cantidad, mensaje);
        System.out.println("✔ Orden de venta enviada.");
    }

    private void handleProducir(String[] tokens)
            throws ProductoNoAutorizadoException,
            RecetaNoEncontradaException,
            IngredientesInsuficientesException {

        if (tokens.length < 3) {
            System.out.println("Uso: producir <producto> <basico|premium>");
            return;
        }

        String producto = tokens[1];
        String tipo = tokens[2].toLowerCase();

        boolean premium = tipo.equals("premium");

        cliente.producir(producto, premium);
        System.out.println("✔ Producción solicitada.");
    }

    private void imprimirAyuda() {
        System.out.println("=== Comandos disponibles ===");
        System.out.println(" login");
        System.out.println(" status");
        System.out.println(" inventario");
        System.out.println(" precios");
        System.out.println(" comprar <producto> <cantidad> [mensaje]");
        System.out.println(" vender <producto> <cantidad> [mensaje]");
        System.out.println(" producir <producto> <basico|premium>");
        System.out.println(" ayuda");
        System.out.println(" exit");
    }

    // ─────────────────────────────
    // UTILIDAD
    // ─────────────────────────────

    private String extraerMensaje(String[] tokens, String linea, int start) {
        if (tokens.length <= start) return "";
        return linea.substring(linea.indexOf(tokens[start])).trim();
    }
}
