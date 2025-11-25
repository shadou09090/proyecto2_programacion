package tech.hellsoft.trading.exception.TradingExceptions;

public class SaldoInsuficienteException extends TradingException {
    private final double saldoDisponible;
    private final double saldoRequerido;

    public SaldoInsuficienteException(String message, double saldoDisponible, double saldoRequerido) {
        super(message);
        this.saldoDisponible = saldoDisponible;
        this.saldoRequerido = saldoRequerido;
    }

    public SaldoInsuficienteException(double saldo, double costo) {
        this("Saldo insuficiente", saldo, costo);
    }

    public double getSaldoDisponible() {
        return saldoDisponible;
    }

    public double getSaldoRequerido() {
        return saldoRequerido;
    }
}
