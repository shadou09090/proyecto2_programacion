package tech.hellsoft.trading.dto.server;

public class InventoryUpdateMessage {

  private final String product;
  private final int quantity;

  public InventoryUpdateMessage(String product, int quantity) {
    this.product = product;
    this.quantity = quantity;
  }

  public String getProduct() {
    return product;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return product + " x" + quantity;
  }
}
