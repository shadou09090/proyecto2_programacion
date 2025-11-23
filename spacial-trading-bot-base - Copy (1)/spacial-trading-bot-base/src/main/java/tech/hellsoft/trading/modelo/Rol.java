package tech.hellsoft.trading.modelo;

public class Rol {

  private final double baseEnergy;
  private final int levelEnergy;
  private final double decay;
  private final double branches;
  private final int maxDepth;

  public Rol(double baseEnergy, int levelEnergy, double decay, double branches, int maxDepth) {
    this.baseEnergy = baseEnergy;
    this.levelEnergy = levelEnergy;
    this.decay = decay;
    this.branches = branches;
    this.maxDepth = maxDepth;
  }

  public double getBaseEnergy() {
    return baseEnergy;
  }

  public int getLevelEnergy() {
    return levelEnergy;
  }

  public double getDecay() {
    return decay;
  }

  public double getBranches() {
    return branches;
  }

  public int getMaxDepth() {
    return maxDepth;
  }
}
