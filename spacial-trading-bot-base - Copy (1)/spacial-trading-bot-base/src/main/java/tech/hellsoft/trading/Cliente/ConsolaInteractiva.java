package tech.hellsoft.trading.Cliente;

import java.util.Scanner;

public class ConsolaInteractiva {

    private ClienteBolsa cliente;
    private Scanner scanner;
    public void iniciar() {
        while (true) {
            System.out.print("\n> ");
            String linea = scanner.nextLine().trim();
            String[] partes = linea.split("\\s+");
            String comando = partes[0].toLowerCase();
            try {
                switch (comando) {
                    case "login": /* ... */ break;
                    case "status": /* ... */ break;

                }
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }


}
