package tech.hellsoft.trading.dto.server;

public class BalanceUpdateMessage {

  private final double balance;

  public BalanceUpdateMessage(double balance) {
    this.balance = balance;
  }

  public double getBalance() {
    return balance;
  }

  @Override
  public String toString() {
    return "Balance=" + balance;
  }
}
