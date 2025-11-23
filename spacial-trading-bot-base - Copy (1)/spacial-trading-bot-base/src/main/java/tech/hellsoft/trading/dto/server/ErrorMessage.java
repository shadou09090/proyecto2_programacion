package tech.hellsoft.trading.dto.server;

/**
 * Represents an error delivered by the trading server.
 */
public class ErrorMessage {

  private final String code;
  private final String reason;

  public ErrorMessage(String code, String reason) {
    this.code = code;
    this.reason = reason;
  }

  public String getCode() {
    return code;
  }

  public String getReason() {
    return reason;
  }
}
