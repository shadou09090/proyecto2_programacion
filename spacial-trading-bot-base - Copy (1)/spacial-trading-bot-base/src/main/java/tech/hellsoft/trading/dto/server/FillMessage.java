package tech.hellsoft.trading.dto.server;

/**
 * Represents a fill confirmation for an order.
 */
public class FillMessage {

  private final String side;
  private final int fillQty;
  private final String product;
  private final double fillPrice;

  public FillMessage(String side, int fillQty, String product, double fillPrice) {
    this.side = side;
    this.fillQty = fillQty;
    this.product = product;
    this.fillPrice = fillPrice;
  }

  public String getSide() {
    return side;
  }

  public int getFillQty() {
    return fillQty;
  }

  public String getProduct() {
    return product;
  }

  public double getFillPrice() {
    return fillPrice;
  }
}
