package tech.hellsoft.trading.dto.server;

/**
 * Simple DTO representing a successful login.
 */
public class LoginOKMessage {

  private final String team;
  private final String species;
  private final double currentBalance;

  public LoginOKMessage(String team, String species, double currentBalance) {
    this.team = team;
    this.species = species;
    this.currentBalance = currentBalance;
  }

  public String getTeam() {
    return team;
  }

  public String getSpecies() {
    return species;
  }

  public double getCurrentBalance() {
    return currentBalance;
  }
}
