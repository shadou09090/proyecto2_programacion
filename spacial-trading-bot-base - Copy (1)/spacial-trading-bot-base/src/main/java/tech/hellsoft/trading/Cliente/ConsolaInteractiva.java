package tech.hellsoft.trading.Cliente;

import java.util.*;

public class ConsolaInteractiva {
    private final ClienteBolsa cliente;
    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, Object> ofertasPendientes = new HashMap<>();

    public ConsolaInteractiva(ClienteBolsa cliente) {
        this.cliente = cliente;
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
        // Implementa la lógica para mostrar el estado actual del cliente
    }

    private void mostrarInventario() {
        // Implementa la lógica para mostrar el inventario
    }

    private void mostrarPrecios() {
        // Implementa la lógica para mostrar los precios actuales
    }

    private void comprar(String[] args) {
        // Implementa la lógica para comprar productos
    }

    private void vender(String[] args) {
        // Implementa la lógica para vender productos
    }

    private void producir(String[] args) {
        // Implementa la lógica para producir productos
    }

    private void mostrarOfertas() {
        // Implementa la lógica para mostrar ofertas pendientes
    }

    private void aceptarOferta(String[] args) {
        // Implementa la lógica para aceptar una oferta
    }

    private void rechazarOferta(String[] args) {
        // Implementa la lógica para rechazar una oferta
    }

    private void manejarSnapshot(String[] args) {
        // Implementa la lógica para guardar/cargar snapshots
    }

    private void resync() {
        // Implementa la lógica para sincronizar eventos perdidos
    }

    private void mostrarAyuda() {
        System.out.println("Comandos disponibles:");
        System.out.println(" status         - Muestra tu situación financiera actual");
        System.out.println(" inventario     - Lista tus productos y cantidades");
        System.out.println(" precios        - Muestra los precios de mercado");
        System.out.println(" comprar        - Compra un producto");
        System.out.println(" vender         - Vende un producto");
        System.out.println(" producir       - Produce unidades de un producto");
        System.out.println(" ofertas        - Lista ofertas pendientes");
        System.out.println(" aceptar        - Acepta una oferta");
        System.out.println(" rechazar       - Rechaza una oferta");
        System.out.println(" snapshot       - Guarda o carga snapshots");
        System.out.println(" resync         - Sincroniza eventos perdidos");
        System.out.println(" ayuda/help     - Muestra esta ayuda");
        System.out.println(" exit           - Cierra el programa");
    }

    private void salir() {
        System.out.println("👋 Cerrando cliente...");
        // Lógica para guardar snapshot final si es necesario
        System.out.println("✅ ¡Hasta luego!");
    }
}
