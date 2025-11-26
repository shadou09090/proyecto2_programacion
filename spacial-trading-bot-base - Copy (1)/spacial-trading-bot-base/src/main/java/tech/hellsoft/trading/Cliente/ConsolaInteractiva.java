package tech.hellsoft.trading.Cliente;

import tech.hellsoft.trading.Cliente.ClienteBolsa;
import tech.hellsoft.trading.Cliente.EstadoCliente;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.dto.server.InventoryUpdateMessage;
import tech.hellsoft.trading.dto.server.FillMessage;
import tech.hellsoft.trading.dto.server.TickerMessage;
import tech.hellsoft.trading.exception.ProduccionException.IngredientesInsuficientesException;
import tech.hellsoft.trading.exception.ProduccionException.RecetaNoEncontradaException;
import tech.hellsoft.trading.exception.TradingExceptions.InventarioInsuficienteException;
import tech.hellsoft.trading.exception.TradingExceptions.ProductoNoAutorizadoException;
import tech.hellsoft.trading.exception.TradingExceptions.SaldoInsuficienteException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * ConsolaInteractiva (implementación según la guía del proyecto).
 * - Usa ClienteBolsa para ejecutar las operaciones reales.
 * - Mantiene un map de ofertas (OfferMessage) que pueden aceptarse/rechazarse.
 * - Implementa snapshot save/load serializando EstadoCliente.
 */
public class ConsolaInteractiva {

    private final ClienteBolsa cliente;
    private final EstadoCliente estado;
    private final Scanner scanner;
    private final Map<String, OfferMessage> ofertasPendientes = new LinkedHashMap<>();
    private boolean running = true;
    private final Path snapshotsDir = Path.of("snapshots");

    public ConsolaInteractiva(ClienteBolsa cliente) {
        this.cliente = cliente;
        this.estado = cliente.getEstado();
        this.scanner = new Scanner(System.in);
        ensureSnapshotsDir();
    }

    private void ensureSnapshotsDir() {
        try {
            if (!Files.exists(snapshotsDir)) {
                Files.createDirectories(snapshotsDir);
            }
        } catch (IOException e) {
            System.err.println("⚠ No se pudo crear carpeta snapshots: " + e.getMessage());
        }
    }

    // Public API para que ClienteBolsa (o el SDK) registre ofertas cuando lleguen.
    public void registrarOferta(OfferMessage offer) {
        if (offer == null) return;
        ofertasPendientes.put(offer.getOfferId(), offer);
        System.out.println("\n📬 Nueva oferta recibida: " + offer);
        System.out.println("💡 Escribe 'ofertas' para verla y 'aceptar " + offer.getOfferId() + "' para aceptar.");
    }

    // ===========================
    // Bucle principal
    // ===========================
    public void iniciar() {
        printWelcome();
        while (running) {
            System.out.print("\n> ");
            if (!scanner.hasNextLine()) break;
            String linea = scanner.nextLine().trim();
            if (linea.isEmpty()) continue;
            String[] partes = linea.split("\\s+");
            String comando = partes[0].toLowerCase();

            try {
                switch (comando) {
                    case "login" -> cmdLogin();
                    case "status" -> cmdStatus();
                    case "inventario" -> cmdInventario();
                    case "precios" -> cmdPrecios();
                    case "comprar" -> cmdComprar(partes);
                    case "vender" -> cmdVender(partes);
                    case "producir" -> cmdProducir(partes);
                    case "ofertas" -> cmdOfertas();
                    case "aceptar" -> cmdAceptar(partes);
                    case "rechazar" -> cmdRechazar(partes);
                    case "snapshot" -> cmdSnapshot(partes);
                    case "resync" -> cmdResync();
                    case "ayuda", "help" -> cmdAyuda();
                    case "exit", "quit", "salir" -> cmdExit();
                    default -> System.out.println("❌ Comando desconocido. Escribe 'ayuda' para ver comandos.");
                }
            } catch (SaldoInsuficienteException e) {
                System.out.println("❌ Saldo insuficiente");
                System.out.println("   " + e.getMessage());
            } catch (InventarioInsuficienteException e) {
                System.out.println("❌ Inventario insuficiente");
                System.out.println("   " + e.getMessage());
            } catch (ProductoNoAutorizadoException e) {
                System.out.println("❌ Producto no autorizado: " + e.getMessage());
            } catch (RecetaNoEncontradaException e) {
                System.out.println("❌ Receta no encontrada: " + e.getMessage());
            } catch (IngredientesInsuficientesException e) {
                System.out.println("❌ Ingredientes insuficientes: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("❌ Error de formato: cantidad debe ser un número entero.");
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }

        // Al salir, intentar guardar snapshot final
        guardarSnapshotAutoAlCerrar();
        System.out.println("👋 Cerrando cliente... ¡Hasta luego!");
    }

    private void printWelcome() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  ConsolaInteractiva - Bolsa Interestelar (CLI)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Escribe 'ayuda' para ver los comandos disponibles.");
    }

    // ===========================
    // Comandos
    // ===========================
    private void cmdLogin() {
        System.out.println("\n🔐 ESTADO DE CONEXIÓN / LOGIN");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        double saldoInicial = estado.getSaldoInicial();
        List<String> productos = estado.getProductosAutorizados();
        System.out.printf("✅ Conectado como equipo (saldo inicial: $%.2f)%n", saldoInicial);
        System.out.println("📦 Productos autorizados: " + productos);
    }

    private void cmdStatus() {
        double saldo = estado.getSaldo();
        double valorInv = 0.0;
        int totalUnidades = 0;

        for (Map.Entry<String, Integer> e : estado.getInventario().entrySet()) {
            double precio = estado.getPreciosActuales().getOrDefault(e.getKey(), 0.0);
            valorInv += e.getValue() * precio;
            totalUnidades += e.getValue();
        }

        double patrimonio = saldo + valorInv;
        double pl = estado.calcularPL();

        System.out.println("\n📊 ESTADO ACTUAL");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("💰 Saldo: $%.2f%n", saldo);
        System.out.printf("📦 Valor inventario: $%.2f%n", valorInv);
        System.out.printf("💎 Patrimonio neto: $%.2f%n", patrimonio);
        System.out.printf("📈 P&L: %+.2f%% %s%n", pl, pl > 0 ? "⬆" : "⬇");
    }

    private void cmdInventario() {
        System.out.println("\n📦 INVENTARIO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (estado.getInventario().isEmpty()) {
            System.out.println("(vacío)");
            return;
        }

        double totalValor = 0.0;
        int totalCant = 0;
        for (Map.Entry<String, Integer> e : estado.getInventario().entrySet()) {
            double precio = estado.getPreciosActuales().getOrDefault(e.getKey(), 0.0);
            double valor = precio * e.getValue();
            System.out.printf("%-12s %4d unidades @ $%.2f = $%.2f%n", e.getKey(), e.getValue(), precio, valor);
            totalValor += valor;
            totalCant += e.getValue();
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("TOTAL: %4d unidades           $%.2f%n", totalCant, totalValor);
    }

    private void cmdPrecios() {
        System.out.println("\n💹 PRECIOS DE MERCADO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (estado.getPreciosActuales().isEmpty()) {
            System.out.println("(esperando tickers...)");
            return;
        }

        for (Map.Entry<String, Double> e : estado.getPreciosActuales().entrySet()) {
            System.out.printf("%-12s $%.2f (mid)%n", e.getKey(), e.getValue());
        }
    }

    private void cmdComprar(String[] partes) throws SaldoInsuficienteException {
        if (partes.length < 3) {
            System.out.println("❌ Uso: comprar <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = partes[1];
        int cantidad = Integer.parseInt(partes[2]);
        String mensaje = partes.length > 3 ? String.join(" ", Arrays.copyOfRange(partes, 3, partes.length)) : "Orden compra CLI";

        // Validaciones mínimas: ticker disponible
        double precio = estado.getPreciosActuales().getOrDefault(producto, -1.0);
        if (precio <= 0.0) {
            System.out.println("❌ No hay precio disponible para " + producto + " (esperando ticker).");
            return;
        }

        System.out.println("\n📤 Orden enviada: COMPRAR " + cantidad + " " + producto);
        // Llamar al cliente para procesar la compra (cliente lanza excepciones según spec)
        cliente.comprar(producto, cantidad, mensaje);
        System.out.println("📌 Orden de compra solicitada. Espera fill (1-10s).");
    }

    private void cmdVender(String[] partes) throws InventarioInsuficienteException {
        if (partes.length < 3) {
            System.out.println("❌ Uso: vender <producto> <cantidad> [mensaje]");
            return;
        }

        String producto = partes[1];
        int cantidad = Integer.parseInt(partes[2]);
        String mensaje = partes.length > 3 ? String.join(" ", Arrays.copyOfRange(partes, 3, partes.length)) : "Orden venta CLI";

        System.out.println("\n📤 Orden enviada: VENDER " + cantidad + " " + producto);
        cliente.vender(producto, cantidad, mensaje);
        System.out.println("📌 Orden de venta solicitada. Espera fill (1-10s).");
    }

    private void cmdProducir(String[] partes) throws ProductoNoAutorizadoException,
            RecetaNoEncontradaException, IngredientesInsuficientesException {
        if (partes.length < 3) {
            System.out.println("❌ Uso: producir <producto> <basico|premium>");
            return;
        }

        String producto = partes[1];
        String tipo = partes[2].toLowerCase();
        boolean premium = tipo.equals("premium");

        System.out.println("\n🏭 Produciendo " + producto + " (" + (premium ? "premium" : "básico") + ")");
        cliente.producir(producto, premium);
        System.out.println("✅ Producción solicitada. Revisa inventario cuando llegue el ack.");
    }

    private void cmdOfertas() {
        System.out.println("\n📬 OFERTAS PENDIENTES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (ofertasPendientes.isEmpty()) {
            System.out.println("(sin ofertas pendientes)");
            return;
        }

        int idx = 1;
        for (OfferMessage o : ofertasPendientes.values()) {
            System.out.println("[" + idx + "] " + "OfferId: " + o.getOfferId());
            System.out.println("    Producto: " + o.getProduct() + " x" + o.getQuantity() + "  Precio: $" + o.getPrice());
            idx++;
        }
    }

    private void cmdAceptar(String[] partes) throws InventarioInsuficienteException {
        if (partes.length < 2) {
            System.out.println("❌ Uso: aceptar <offerId>");
            return;
        }
        String offerId = partes[1];
        OfferMessage offer = ofertasPendientes.get(offerId);
        if (offer == null) {
            System.out.println("❌ Oferta no encontrada: " + offerId);
            return;
        }

        System.out.println("\n✅ Aceptando oferta " + offerId + "...");
        // La guía sugiere aceptar la oferta vendiendo el producto al comprador.
        // Llamamos a cliente.vender(product, qty, "Aceptando oferta " + offerId)
        cliente.vender(offer.getProduct(), offer.getQuantity(), "Aceptando oferta " + offerId);
        // Si no lanza excepciones, asumimos orden enviada.
        ofertasPendientes.remove(offerId);
        System.out.println("📌 Oferta aceptada. Espera fill con confirmación.");
    }

    private void cmdRechazar(String[] partes) {
        if (partes.length < 2) {
            System.out.println("❌ Uso: rechazar <offerId> [motivo]");
            return;
        }
        String offerId = partes[1];
        String motivo = partes.length > 2 ? String.join(" ", Arrays.copyOfRange(partes, 2, partes.length)) : "Sin motivo";
        OfferMessage offer = ofertasPendientes.remove(offerId);
        if (offer == null) {
            System.out.println("❌ Oferta no encontrada: " + offerId);
            return;
        }
        System.out.println("❌ Oferta " + offerId + " rechazada. Motivo: " + motivo);
        // (Opcional) podrías notificar al servidor que rechazas, pero la guía permite simplemente removerla.
    }

    // snapshot save / snapshot load
    private void cmdSnapshot(String[] partes) {
        if (partes.length < 2) {
            System.out.println("❌ Uso: snapshot <save|load>");
            return;
        }
        String sub = partes[1].toLowerCase();
        if (sub.equals("save")) {
            snapshotSave();
        } else if (sub.equals("load")) {
            snapshotLoadInteractive();
        } else {
            System.out.println("❌ Uso: snapshot save | snapshot load");
        }
    }

    private void snapshotSave() {
        long ts = System.currentTimeMillis();
        String fileName = "snapshot_" + ts + ".bin";
        Path f = snapshotsDir.resolve(fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(f))) {
            oos.writeObject(estado);
            System.out.println("💾 Guardando snapshot...");
            System.out.println("✅ Snapshot guardado: " + f.toString());
        } catch (IOException e) {
            System.out.println("❌ Error guardando snapshot: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void snapshotLoadInteractive() {
        try {
            List<Path> snaps = listSnapshots();
            if (snaps.isEmpty()) {
                System.out.println("📂 No hay snapshots disponibles.");
                return;
            }

            System.out.println("📂 Snapshots disponibles:");
            int i = 1;
            for (Path p : snaps) {
                System.out.println(i + ". " + p.getFileName());
                i++;
            }

            System.out.print("Selecciona snapshot (número): ");
            String sel = scanner.nextLine().trim();
            int idx = Integer.parseInt(sel) - 1;
            if (idx < 0 || idx >= snaps.size()) {
                System.out.println("❌ Selección inválida.");
                return;
            }

            Path chosen = snaps.get(idx);
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(chosen))) {
                EstadoCliente loaded = (EstadoCliente) ois.readObject();
                // Reemplazamos el estado actual (solo serializamos/deserializamos el objeto)
                // *** IMPORTANTE: sincroniza con la referencia del cliente si lo necesitas.
                // Para simplicidad aquí copiamos datos relevantes al estado actual.
                estado.setSaldo(loaded.getSaldo());
                estado.setSaldoInicial(loaded.getSaldoInicial());
                estado.setInventario(loaded.getInventario());
                estado.setPreciosActuales(loaded.getPreciosActuales());
                estado.setRecetas(loaded.getRecetas());
                estado.setProductosAutorizados(loaded.getProductosAutorizados());
                estado.setRol(loaded.getRol());

                System.out.println("✅ Estado cargado correctamente desde " + chosen.getFileName());
                System.out.printf("💰 Saldo: $%.2f%n", estado.getSaldo());
                System.out.printf("📈 P&L: %+.2f%%%n", estado.calcularPL());
            } catch (ClassNotFoundException e) {
                System.out.println("❌ Error cargando snapshot (clase no encontrada): " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("❌ Error leyendo snapshots: " + e.getMessage());
        }
    }

    private List<Path> listSnapshots() throws IOException {
        if (!Files.exists(snapshotsDir)) return Collections.emptyList();
        try (var s = Files.list(snapshotsDir)) {
            List<Path> list = s.filter(p -> p.getFileName().toString().startsWith("snapshot_"))
                    .sorted(Comparator.reverseOrder()).toList();
            return list;
        }
    }

    private void cmdResync() {
        System.out.println("\n🔄 RESYNC - Sincronización de eventos perdidos");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1) Asegúrate de haber cargado el snapshot correcto (snapshot load).");
        System.out.println("2) Ejecuta 'login' para reconectar al servidor.");
        System.out.println("3) El servidor enviará los FILLs pendientes y el SDK llamará a tus callbacks.");
        System.out.println("🔔 Nota: la resync real depende del soporte del servidor/SDK. Si implementaste");
        System.out.println("   un método en tu ClienteBolsa para solicitar resync, llámalo ahora.");
        // Si ClienteBolsa tiene un método resync(), intentamos llamarlo por reflexión (si existe).
        try {
            var m = cliente.getClass().getMethod("resync");
            m.invoke(cliente);
            System.out.println("✅ Llamado a cliente.resync() (si existe) fue invocado.");
        } catch (NoSuchMethodException ignored) {
            System.out.println("ℹ ClienteBolsa no implementa resync() - realiza resync manualmente.");
        } catch (Exception e) {
            System.out.println("❌ Error llamando resync en ClienteBolsa: " + e.getMessage());
        }
    }

    private void cmdAyuda() {
        System.out.println("\n📚 AYUDA - Comandos disponibles");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("login                 - Mostrar estado de conexión y equipo");
        System.out.println("status                - Mostrar saldo, inventario, P&L");
        System.out.println("inventario            - listar inventario con valores");
        System.out.println("precios               - listar precios actuales (mid)");
        System.out.println("comprar <prod> <qty> [mensaje]");
        System.out.println("vender <prod> <qty> [mensaje]");
        System.out.println("producir <prod> <basico|premium>");
        System.out.println("ofertas               - listar ofertas pendientes");
        System.out.println("aceptar <offerId>     - aceptar oferta (vende al comprador)");
        System.out.println("rechazar <offerId> [motivo]");
        System.out.println("snapshot save          - guardar snapshot binario");
        System.out.println("snapshot load          - listar y cargar snapshot");
        System.out.println("resync                - solicitar resync (ver notas)");
        System.out.println("ayuda|help            - mostrar esta ayuda");
        System.out.println("exit|quit|salir       - salir y guardar snapshot final");
    }

    private void cmdExit() {
        running = false;
    }

    private void guardarSnapshotAutoAlCerrar() {
        try {
            long ts = System.currentTimeMillis();
            String fileName = "snapshot_final_" + ts + ".bin";
            Path f = snapshotsDir.resolve(fileName);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(f))) {
                oos.writeObject(estado);
                System.out.println("💾 Snapshot final guardado: " + f.toString());
            }
        } catch (Exception e) {
            // no bloquear el cierre por fallo en snapshot
            System.err.println("⚠ Error guardando snapshot final: " + e.getMessage());
        }
    }

    // ===========================
    // Métodos utilitarios (por si quieres llamar desde ClienteBolsa)
    // ===========================
    public Map<String, OfferMessage> getOfertasPendientes() {
        return Collections.unmodifiableMap(ofertasPendientes);
    }

    public void clearOfertas() {
        ofertasPendientes.clear();
    }

    // Conveniencia para tests: exponer scanner (no recomendado en producción)
    public Scanner getScanner() {
        return scanner;
    }
}
