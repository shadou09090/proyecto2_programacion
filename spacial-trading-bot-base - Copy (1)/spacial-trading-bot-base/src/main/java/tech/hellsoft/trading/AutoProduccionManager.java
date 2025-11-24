package tech.hellsoft.trading;

import tech.hellsoft.trading.Cliente.ClienteBolsa;
import tech.hellsoft.trading.modelo.EstadoCliente;
import tech.hellsoft.trading.modelo.Receta;
import tech.hellsoft.trading.util.RecetaValidator;

public class AutoProduccionManager extends TareaAutomatica {

  private final ClienteBolsa cliente;
  private final String productoBasico;
  private final String productoPremium;

  public AutoProduccionManager(ClienteBolsa cliente, String productoBasico, String productoPremium) {
    this.cliente = cliente;
    this.productoBasico = productoBasico;
    this.productoPremium = productoPremium;
  }

  @Override
  protected void ejecutar() {
    try {
      EstadoCliente estado = cliente.getEstado();
      Receta recetaPremium = estado.getRecetas().get(productoPremium);
      boolean puedePremium = RecetaValidator.puedeProducir(recetaPremium, estado.getInventario());
      if (puedePremium) {
        cliente.producir(productoPremium, true);
        System.out.println("[AUTO] Producción premium: " + productoPremium);
        return;
      }
      cliente.producir(productoBasico, false);
      System.out.println("[AUTO] Producción básica: " + productoBasico);
      int cantidad = estado.getInventario().getOrDefault(productoBasico, 0);
      if (cantidad <= 0) {
        return;
      }
      cliente.vender(productoBasico, cantidad, "Auto-venta");
      System.out.println("[AUTO] Vendidas " + cantidad + " unidades de " + productoBasico);
    } catch (Exception e) {
      System.out.println("[AUTO] Error: " + e.getMessage());
    }
  }
}
