package tech.hellsoft.trading.exception;

import tech.hellsoft.trading.exception.ConfiguracionException.ConfiguracionException;

public class ConfiguracionInvalidaException extends ConfiguracionException {

    public ConfiguracionInvalidaException(String message) {
        super(message);
    }

    public ConfiguracionInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}