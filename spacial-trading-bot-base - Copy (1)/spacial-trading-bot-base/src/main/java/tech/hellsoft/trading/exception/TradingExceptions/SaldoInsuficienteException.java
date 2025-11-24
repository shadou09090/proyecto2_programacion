package tech.hellsoft.trading.exception.TradingExceptions;

public class SaldoInsuficienteException extends TradingException {
    public SaldoInsuficienteException(String message) {
        super(message);
    }


    public SaldoInsuficienteException(double saldo, double costo) {
        super("G");
    }
}
