package tech.hellsoft.trading.Cliente;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsolaInteractiva {
    private final ClienteBolsa cliente;
    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, Object> ofertasPendientes = new HashMap<>();

    // Nuevo estado interno
    private double balance = 1000.0;
    private final Map<String, Integer> inventario = new HashMap<>();
    private final Map<String, Double> precios = new HashMap<>();
    private final AtomicInteger ofertaIdGen = new AtomicInteger(1);

    public ConsolaInteractiva(ClienteBolsa cliente) {
        this.cliente = cliente;
        // Inicializar algunos precios por defecto
        precios.put("hierro", 10.0);
        precios.put("madera", 5.0);
        precios.put("oro", 50.0);
        precios.put("comida", 2.5);
    }

    public void iniciar() {
        System.out.println("Bienvenido a la consola interactiva. Escribe 'ayuda' para ver los comandos.");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] args = input.split("\\s+");
            String comando = args[0].toLowerCase();
            try {
                switch (comando) {
                    case "status": mostrarStatus(); break;
                    case "inventario": mostrarInventario(); break;
                    case "precios": mostrarPrecios(); break;
                    case "comprar": comprar(args); break;
                    case "vender": vender(args); break;
                    case "producir": producir(args); break;
                    case "ofertas": mostrarOfertas(); break;
                    case "ofertar": ofertar(args); break;
                    case "aceptar": aceptarOferta(args); break;
                    case "rechazar": rechazarOferta(args); break;
                    case "snapshot": manejarSnapshot(args); break;
                    case "resync": resync(); break;
                    case "ayuda":
                    case "help": mostrarAyuda(); break;
                    case "exit": salir(); return;
                    default: System.out.println("❓ Comando no reconocido. Escribe 'ayuda' para ver los comandos.");
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                System.out.println("❌ Error: " + msg);
            }
        }
    }

    private void mostrarStatus() {
        System.out.println("--- Estado del cliente ---");
        System.out.printf(" Fecha: %s%n", LocalDateTime.now());
        System.out.printf(" Saldo: %.2f%n", balance);
        int totalItems = inventario.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf(" Items en inventario: %d (distintos: %d)%n", totalItems, inventario.size());
    }

    private void mostrarInventario() {
        System.out.println("--- Inventario ---");
        if (inventario.isEmpty()) {
            System.out.println(" (vacío)");
            return;
        }
        inventario.forEach((k, v) -> System.out.printf(" %s: %d%n", k, v));
    }

    private void mostrarPrecios() {
        System.out.println("--- Precios de mercado ---");
        precios.forEach((k, v) -> System.out.printf(" %s: %.2f%n", k, v));
    }

    private void comprar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: comprar <producto> <cantidad> [precio_por_unidad]");
            return;
        }
        String prod = args[1].toLowerCase();
        int qty = parseInt(args[2], -1);
        if (qty <= 0) {
            System.out.println("Cantidad inválida.");
            return;
        }
        double unit = precios.getOrDefault(prod, Double.NaN);
        if (args.length >= 4) {
            double p = parseDouble(args[3], Double.NaN);
            if (!Double.isNaN(p) && p > 0) unit = p;
        }
        if (Double.isNaN(unit) || unit <= 0) {
            System.out.println("Precio desconocido para el producto. Añádelo a precios antes o especifica precio.");
            return;
        }
        double cost = unit * qty;
        if (cost > balance) {
            System.out.println("Fondos insuficientes. Necesitas " + cost + " pero tienes " + balance);
            return;
        }
        balance -= cost;
        inventario.put(prod, inventario.getOrDefault(prod, 0) + qty);
        precios.put(prod, unit); // actualizar precio de referencia
        System.out.printf("Compra realizada: %d x %s a %.2f (total %.2f). Saldo: %.2f%n", qty, prod, unit, cost, balance);
    }

    private void vender(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: vender <producto> <cantidad> [precio_por_unidad]");
            return;
        }
        String prod = args[1].toLowerCase();
        int qty = parseInt(args[2], -1);
        if (qty <= 0) {
            System.out.println("Cantidad inválida.");
            return;
        }
        int have = inventario.getOrDefault(prod, 0);
        if (have < qty) {
            System.out.println("No tienes suficientes unidades. Tienes: " + have);
            return;
        }
        double unit = precios.getOrDefault(prod, Double.NaN);
        if (args.length >= 4) {
            double p = parseDouble(args[3], Double.NaN);
            if (!Double.isNaN(p) && p > 0) unit = p;
        }
        if (Double.isNaN(unit) || unit <= 0) {
            System.out.println("Precio desconocido para el producto. Especifica el precio.");
            return;
        }
        double gained = unit * qty;
        inventario.put(prod, have - qty);
        if (inventario.get(prod) == 0) inventario.remove(prod);
        balance += gained;
        precios.put(prod, unit); // actualizar precio de referencia
        System.out.printf("Venta realizada: %d x %s a %.2f (ganancia %.2f). Saldo: %.2f%n", qty, prod, unit, gained, balance);
    }

    private void producir(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: producir <producto> <cantidad> [costo_por_unidad]");
            return;
        }
        String prod = args[1].toLowerCase();
        int qty = parseInt(args[2], -1);
        if (qty <= 0) {
            System.out.println("Cantidad inválida.");
            return;
        }
        double costUnit = 0.0;
        if (args.length >= 4) {
            costUnit = parseDouble(args[3], 0.0);
            if (costUnit < 0) costUnit = 0.0;
        }
        double totalCost = costUnit * qty;
        if (totalCost > balance) {
            System.out.println("Fondos insuficientes para producir. Requerido: " + totalCost);
            return;
        }
        balance -= totalCost;
        inventario.put(prod, inventario.getOrDefault(prod, 0) + qty);
        System.out.printf("Producido: %d x %s (costo %.2f). Saldo: %.2f%n", qty, prod, totalCost, balance);
    }

    private void mostrarOfertas() {
        System.out.println("--- Ofertas pendientes ---");
        if (ofertasPendientes.isEmpty()) {
            System.out.println(" (ninguna)");
            return;
        }
        ofertasPendientes.forEach((id, o) -> {
            Offer of = (Offer) o;
            System.out.printf(" %s | %s | %s x%d @ %.2f | %s%n", id, of.type, of.product, of.quantity, of.price, of.from);
        });
    }

    // Nuevo comando: ofertar (crear una oferta)
    private void ofertar(String[] args) {
        // Sintaxis: ofertar buy|sell producto cantidad precio [de]
        if (args.length < 5) {
            System.out.println("Uso: ofertar <buy|sell> <producto> <cantidad> <precio> [remitente]");
            return;
        }
        String type = args[1].toLowerCase();
        if (!type.equals("buy") && !type.equals("sell")) {
            System.out.println("Tipo inválido: debe ser 'buy' o 'sell'.");
            return;
        }
        String prod = args[2].toLowerCase();
        int qty = parseInt(args[3], -1);
        double price = parseDouble(args[4], -1.0);
        String from = args.length >= 6 ? args[5] : "externo";
        if (qty <= 0 || price <= 0) {
            System.out.println("Cantidad o precio inválidos.");
            return;
        }
        String id = String.valueOf(ofertaIdGen.getAndIncrement());
        Offer of = new Offer(id, type, prod, qty, price, from);
        ofertasPendientes.put(id, of);
        System.out.printf("Oferta creada id=%s: %s %s x%d @ %.2f por %s%n", id, type, prod, qty, price, from);
    }

    private void aceptarOferta(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: aceptar <id_oferta>");
            return;
        }
        String id = args[1];
        Object o = ofertasPendientes.get(id);
        if (o == null) {
            System.out.println("Oferta no encontrada: " + id);
            return;
        }
        Offer of = (Offer) o;
        if (of.type.equals("buy")) {
            // otro quiere comprar: nosotros vendemos
            int have = inventario.getOrDefault(of.product, 0);
            if (have < of.quantity) {
                System.out.println("No tienes suficientes unidades para aceptar esta oferta.");
                return;
            }
            inventario.put(of.product, have - of.quantity);
            if (inventario.get(of.product) == 0) inventario.remove(of.product);
            balance += of.price * of.quantity;
            ofertasPendientes.remove(id);
            System.out.printf("Oferta %s aceptada: vendiste %d x %s por %.2f. Saldo: %.2f%n", id, of.quantity, of.product, of.price * of.quantity, balance);
        } else {
            // otro quiere vender: nosotros compramos
            double total = of.price * of.quantity;
            if (balance < total) {
                System.out.println("Fondos insuficientes para aceptar la oferta.");
                return;
            }
            balance -= total;
            inventario.put(of.product, inventario.getOrDefault(of.product, 0) + of.quantity);
            ofertasPendientes.remove(id);
            System.out.printf("Oferta %s aceptada: compraste %d x %s por %.2f. Saldo: %.2f%n", id, of.quantity, of.product, total, balance);
        }
    }

    private void rechazarOferta(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: rechazar <id_oferta>");
            return;
        }
        String id = args[1];
        if (ofertasPendientes.remove(id) != null) {
            System.out.println("Oferta " + id + " rechazada y eliminada.");
        } else {
            System.out.println("Oferta no encontrada: " + id);
        }
    }

    private void manejarSnapshot(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: snapshot <save|load> [ruta]");
            return;
        }
        String op = args[1].toLowerCase();
        String ruta = args.length >= 3 ? args[2] : "snapshot.bin";
        try {
            if (op.equals("save")) {
                saveSnapshot(ruta);
                System.out.println("Snapshot guardado en " + ruta);
            } else if (op.equals("load")) {
                loadSnapshot(ruta);
                System.out.println("Snapshot cargado desde " + ruta);
            } else {
                System.out.println("Operación inválida. Usa save o load.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error en snapshot: " + e.getMessage());
        }
    }

    private void resync() {
        // No hay sistema de eventos aquí; mostrar mensaje y continuar.
        System.out.println("Resync ejecutado: revisión rápida completada (sin cambios).");
    }

    private void mostrarAyuda() {
        System.out.println("Comandos disponibles:");
        System.out.println(" status         - Muestra tu situación financiera actual");
        System.out.println(" inventario     - Lista tus productos y cantidades");
        System.out.println(" precios        - Muestra los precios de mercado");
        System.out.println(" comprar        - Compra un producto: comprar <producto> <cantidad> [precio]");
        System.out.println(" vender         - Vende un producto: vender <producto> <cantidad> [precio]");
        System.out.println(" producir       - Produce unidades: producir <producto> <cantidad> [costo_por_unidad]");
        System.out.println(" ofertar        - Crea oferta: ofertar <buy|sell> <producto> <cantidad> <precio> [remitente]");
        System.out.println(" ofertas        - Lista ofertas pendientes");
        System.out.println(" aceptar        - Acepta una oferta: aceptar <id>");
        System.out.println(" rechazar       - Rechaza una oferta: rechazar <id>");
        System.out.println(" snapshot       - Guarda o carga snapshots: snapshot <save|load> [ruta]");
        System.out.println(" resync         - Sincroniza eventos perdidos (no-op aquí)");
        System.out.println(" ayuda/help     - Muestra esta ayuda");
        System.out.println(" exit           - Cierra el programa");
    }

    private void salir() {
        System.out.println("👋 Cerrando cliente...");
        try {
            saveSnapshot("snapshot_autosave.bin");
            System.out.println("Snapshot automático guardado.");
        } catch (Exception e) {
            // ignorar errores al cerrar
        }
        System.out.println("✅ ¡Hasta luego!");
    }

    // Helpers y serialización

    private void saveSnapshot(String path) throws IOException {
        Snapshot s = new Snapshot(balance, inventario, precios, ofertasPendientes, ofertaIdGen.get());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(path)))) {
            oos.writeObject(s);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSnapshot(String path) throws IOException, ClassNotFoundException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) throw new FileNotFoundException("Archivo no encontrado: " + path);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(p))) {
            Object obj = ois.readObject();
            if (obj instanceof Snapshot) {
                Snapshot s = (Snapshot) obj;
                this.balance = s.balance;
                this.inventario.clear();
                this.inventario.putAll(s.inventario);
                this.precios.clear();
                this.precios.putAll(s.precios);
                this.ofertasPendientes.clear();
                this.ofertasPendientes.putAll(s.ofertas);
                this.ofertaIdGen.set(s.nextOfferId);
            } else {
                throw new IOException("Contenido de snapshot inválido.");
            }
        }
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private double parseDouble(String s, double fallback) {
        try { return Double.parseDouble(s); } catch (Exception e) { return fallback; }
    }

    // Clase Offer simple serializable
    private static class Offer implements Serializable {
        final String id;
        final String type; // buy o sell
        final String product;
        final int quantity;
        final double price;
        final String from;
        Offer(String id, String type, String product, int quantity, double price, String from) {
            this.id = id;
            this.type = type;
            this.product = product;
            this.quantity = quantity;
            this.price = price;
            this.from = from;
        }
    }

    // Clase Snapshot para guardar/recuperar estado
    private static class Snapshot implements Serializable {
        final double balance;
        final Map<String, Integer> inventario;
        final Map<String, Double> precios;
        final Map<String, Object> ofertas;
        final int nextOfferId;
        Snapshot(double balance, Map<String, Integer> inventario, Map<String, Double> precios, Map<String, Object> ofertas, int nextOfferId) {
            this.balance = balance;
            // serializar copias para evitar referencias compartidas
            this.inventario = new HashMap<>(inventario);
            this.precios = new HashMap<>(precios);
            this.ofertas = new HashMap<>(ofertas);
            this.nextOfferId = nextOfferId;
        }
    }
}
