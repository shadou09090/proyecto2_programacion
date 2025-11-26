package tech.hellsoft.trading.exception.ConfiguracionException;

public class SnapshotCorruptoException extends ConfiguracionException {

    public SnapshotCorruptoException(String message) {
        super(message);
    }

    public SnapshotCorruptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
