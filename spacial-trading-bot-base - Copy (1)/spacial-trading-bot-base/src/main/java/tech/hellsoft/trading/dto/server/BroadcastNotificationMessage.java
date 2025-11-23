package tech.hellsoft.trading.dto.server;

public class BroadcastNotificationMessage {

  private final String message;

  public BroadcastNotificationMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
