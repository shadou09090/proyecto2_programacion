package tech.hellsoft.trading.dto.server;

/**
 * Snapshot of current market prices.
 */
public class TickerMessage {

  private final String product;
  private final double bestBid;
  private final double bestAsk;

  public TickerMessage(String product, double bestBid, double bestAsk) {
    this.product = product;
    this.bestBid = bestBid;
    this.bestAsk = bestAsk;
  }

  public String getProduct() {
    return product;
  }

  public double getBestBid() {
    return bestBid;
  }

  public double getBestAsk() {
    return bestAsk;
  }

  public double getMid() {
    return (bestBid + bestAsk) / 2.0;
  }
}
