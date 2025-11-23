package tech.hellsoft.trading.dto.server;

public class OfferMessage {

  private final String offerId;
  private final String product;
  private final int quantity;
  private final double price;

  public OfferMessage(String offerId, String product, int quantity, double price) {
    this.offerId = offerId;
    this.product = product;
    this.quantity = quantity;
    this.price = price;
  }

  public String getOfferId() {
    return offerId;
  }

  public String getProduct() {
    return product;
  }

  public int getQuantity() {
    return quantity;
  }

  public double getPrice() {
    return price;
  }

  @Override
  public String toString() {
    return offerId + " - " + product + " x" + quantity + " @ " + price;
  }
}
