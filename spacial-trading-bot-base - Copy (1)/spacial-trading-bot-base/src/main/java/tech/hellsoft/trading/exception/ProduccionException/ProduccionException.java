package tech.hellsoft.trading.exception.ProduccionException;

public abstract class ProduccionException extends RuntimeException {
    public ProduccionException(String message) {
        super("message");
    }
}
