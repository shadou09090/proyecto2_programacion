package tech.hellsoft.trading;

public class AutoProduccionManager extends TareaAutomatica {
    private ClienteBolsa cliente;
    private String productoBasico; // ej: "PALTA-OIL"
    private String productoPremium; // ej: "GUACA"
    public AutoProduccionManager(ClienteBolsa cliente,
                                 String productoBasico,
                                 String productoPremium) {
        this.cliente = cliente;
        this.productoBasico = productoBasico;
        this.productoPremium = productoPremium;
    }
    @Override
    protected void ejecutar() {
        try {
            EstadoCliente estado = cliente.getEstado();
// Estrategia 1: Intentar premium primero
            Receta recetaPremium = estado.getRecetas().get(productoPremium);
            boolean puedePremium = RecetaValidator.puedeProducir(
                    recetaPremium,
                    estado.getInventario()
            );
            if (puedePremium) {
// PRODUCIR PREMIUM (no vender automáticamente)
                cliente.producir(productoPremium, true);
                System.out.println("[AUTO] Producción premium: " + productoPremium);
            } else {
// PRODUCIR BÁSICO + VENDER INMEDIATAMENTE
                cliente.producir(productoBasico, false);
                System.out.println("[AUTO] Producción básica: " + productoBasico);
// Vender todo el básico para conseguir capital
                int cantidad = estado.getInventario()
                        .getOrDefault(productoBasico, 0);
                if (cantidad > 0) {
                    cliente.vender(productoBasico, cantidad, "Auto-venta");
                    System.out.println("[AUTO] Vendidas " + cantidad +
                            " unidades de " + productoBasico);
                }
            }
        } catch (Exception e) {
            System.out.println("[AUTO] Error: " + e.getMessage());
        }
    }
}
// En tu Main (después del login):
AutoProduccionManager autoProductor = new AutoProduccionManager(
        cliente,
        "PALTA-OIL", // básico
        "GUACA" // premium
);
autoProductor.iniciar(60); // Cada 60 segundos
System.out.println("✅ Auto-producción activada (cada 60s)");