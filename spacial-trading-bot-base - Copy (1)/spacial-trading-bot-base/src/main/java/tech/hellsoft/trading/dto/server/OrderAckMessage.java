package tech.hellsoft.trading.dto.server;

public class OrderAckMessage {

  private final String orderId;
  private final String status;

  public OrderAckMessage(String orderId, String status) {
    this.orderId = orderId;
    this.status = status;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return orderId + " - " + status;
  }
}
