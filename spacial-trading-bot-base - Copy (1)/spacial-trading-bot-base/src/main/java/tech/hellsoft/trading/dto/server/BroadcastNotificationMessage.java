package tech.hellsoft.trading.dto.server;

public class BroadcastNotificationMessage {

    private String message;

    public BroadcastNotificationMessage() {}

    public BroadcastNotificationMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
