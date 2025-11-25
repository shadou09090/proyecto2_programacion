package tech.hellsoft.trading.Cliente;

import java.util.*;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;
import tech.hellsoft.trading.util.SnapshotManager;

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

                    case "ofertas":
                        handleOfertas();
                        break;

                    case "aceptar":
                        handleAceptar(tokens);
                        break;

                    case "rechazar":
                        handleRechazar(tokens, linea);
                        break;

                    case "snapshot":
                        if (tokens.length < 2) {
                            System.out.println("Uso: snapshot <save|load>");
                            break;
                        }
                        if (tokens[1].equalsIgnoreCase("save")) handleSnapshotSave();
                        else if (tokens[1].equalsIgnoreCase("load")) handleSnapshotLoad();
                        else System.out.println("Uso: snapshot <save|load>");
                        break;

                    case "ayuda":
                    case "help":
                        imprimirAyuda();
                        break;

                    case "exit":
                        System.out.println("Cerrando cliente...");
                        return;

                    default:
                        System.out.println("Comando desconocido. Usa 'ayuda'.");
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

    //─────────────────────────────
    // COMANDOS
    //─────────────────────────────

    private void handleLogin() {
        System.out.println("Esperando LOGIN_OK del servidor...");
        System.out.println("Si tu conector está corriendo, el callback onLoginOk se ejecutará.");
    }

    private void handleStatus() {
        EstadoCliente est = cliente.getEstado();
        double saldo = est.getSaldo();
        double pl = est.calcularPLPorcentaje();

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

    private void handleOfertas() {
        Map<String, OfferMessage> ofertas = cliente.getOfertas();
        if (ofertas.isEmpty()) {
            System.out.println("No hay ofertas pendientes.");
            return;
        }

        System.out.println("=== OFERTAS PENDIENTES ===");
        ofertas.forEach((id, oferta) -> {
            System.out.println(id + " -> " + oferta);
        });
    }

    private void handleAceptar(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            System.out.println("Uso: aceptar <offerId>");
            return;
        }
        String id = tokens[1];
        cliente.aceptarOferta(id);
        System.out.println("✔ Oferta aceptada: " + id);
    }

    private void handleRechazar(String[] tokens, String linea) throws Exception {
        if (tokens.length < 2) {
            System.out.println("Uso: rechazar <offerId> [motivo]");
            return;
        }

        String id = tokens[1];
        String motivo = extraerMensaje(tokens, linea, 2);

        cliente.rechazarOferta(id);
        System.out.println("✔ Oferta rechazada: " + id);
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

    private void handleSnapshotSave() throws Exception {
        System.out.print("Nombre del snapshot: ");
        String nombre = scanner.nextLine().trim();

        if (nombre.isEmpty()) nombre = "snapshot-" + System.currentTimeMillis();

        SnapshotManager.guardar(cliente.getEstado(), nombre + ".bin");
        System.out.println("✔ Snapshot guardado: " + nombre + ".bin");
    }

    private void handleSnapshotLoad() throws Exception {
        System.out.print("Archivo snapshot a cargar: ");
        String nombre = scanner.nextLine().trim();

        var estadoCargado = SnapshotManager.cargar(nombre);
        cliente.restaurarEstado(estadoCargado);

        System.out.println("✔ Snapshot cargado: " + nombre);
    }

    //─────────────────────────────
    // UTILIDAD
    //─────────────────────────────

    private void imprimirAyuda() {
        System.out.println("=== Comandos disponibles ===");
        System.out.println(" login");
        System.out.println(" status");
        System.out.println(" inventario");
        System.out.println(" precios");
        System.out.println(" comprar <producto> <cantidad> [mensaje]");
        System.out.println(" vender <producto> <cantidad> [mensaje]");
        System.out.println(" producir <producto> <basico|premium>");
        System.out.println(" ofertas");
        System.out.println(" aceptar <id>");
        System.out.println(" rechazar <id> [motivo]");
        System.out.println(" snapshot save");
        System.out.println(" snapshot load");
        System.out.println(" ayuda");
        System.out.println(" exit");
    }

    private String extraerMensaje(String[] tokens, String linea, int start) {
        if (tokens.length <= start) return "";
        return linea.substring(linea.indexOf(tokens[start])).trim();
    }
}
