package tech.hellsoft.trading.dto.server;

public class EventDeltaMessage {

  private final String description;

  public EventDeltaMessage(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return description;
  }
}
