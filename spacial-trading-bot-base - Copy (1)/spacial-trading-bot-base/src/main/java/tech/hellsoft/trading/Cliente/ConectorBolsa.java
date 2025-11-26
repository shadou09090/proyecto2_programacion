package tech.hellsoft.trading.Cliente;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.hellsoft.trading.EventListener;
import tech.hellsoft.trading.config.ConectorConfig;
import tech.hellsoft.trading.dto.client.AcceptOfferMessage;
import tech.hellsoft.trading.dto.client.CancelMessage;
import tech.hellsoft.trading.dto.client.LoginMessage;
import tech.hellsoft.trading.dto.client.OrderMessage;
import tech.hellsoft.trading.dto.client.ProductionUpdateMessage;
import tech.hellsoft.trading.dto.server.BalanceUpdateMessage;
import tech.hellsoft.trading.dto.server.BroadcastNotificationMessage;
import tech.hellsoft.trading.dto.server.ErrorMessage;
import tech.hellsoft.trading.dto.server.EventDeltaMessage;
import tech.hellsoft.trading.dto.server.FillMessage;
import tech.hellsoft.trading.dto.server.GlobalPerformanceReportMessage;
import tech.hellsoft.trading.dto.server.InventoryUpdateMessage;
import tech.hellsoft.trading.dto.server.LoginOKMessage;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.dto.server.OrderAckMessage;
import tech.hellsoft.trading.dto.server.PongMessage;
import tech.hellsoft.trading.dto.server.TickerMessage;
import tech.hellsoft.trading.enums.ConnectionState;
import tech.hellsoft.trading.enums.ErrorCode;
import tech.hellsoft.trading.enums.MessageType;
import tech.hellsoft.trading.exception.ConexionFallidaException;
import tech.hellsoft.trading.internal.connection.HeartbeatManager;
import tech.hellsoft.trading.internal.connection.WebSocketHandler;
import tech.hellsoft.trading.internal.routing.MessageRouter;
import tech.hellsoft.trading.internal.routing.MessageSequencer;
import tech.hellsoft.trading.internal.serialization.JsonSerializer;
import tech.hellsoft.trading.tasks.TareaAutomatica;
import tech.hellsoft.trading.tasks.TareaAutomaticaManager;

public class ConectorBolsa {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(tech.hellsoft.trading.ConectorBolsa.class);
    private final ConectorConfig config;
    private final List<EventListener> listeners;
    private final MessageSequencer sequencer;
    private final MessageRouter router;
    private final ExecutorService callbackExecutor;
    private final Semaphore sendLock;
    private final TareaAutomaticaManager tareaManager;
    private volatile ConnectionState state;
    private volatile WebSocket webSocket;
    private HeartbeatManager heartbeatManager;
    private volatile CompletableFuture<LoginOKMessage> loginFuture;

    public ConectorBolsa(ConectorConfig config) {
        this.listeners = new CopyOnWriteArrayList();
        this.sequencer = new MessageSequencer();
        this.router = new MessageRouter();
        this.callbackExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.sendLock = new Semaphore(1);
        this.tareaManager = new TareaAutomaticaManager();
        this.state = ConnectionState.DISCONNECTED;
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        } else {
            config.validate();
            this.config = config;
        }
    }

    public ConectorBolsa() {
        this(ConectorConfig.defaultConfig());
    }

    public void addListener(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            this.listeners.add(listener);
            log.debug("Added listener: {}", listener.getClass().getSimpleName());
        }
    }

    public void removeListener(EventListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
            log.debug("Removed listener: {}", listener.getClass().getSimpleName());
        }
    }

    public void registrarTarea(TareaAutomatica tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("tarea cannot be null");
        } else {
            this.tareaManager.registrar(tarea);
            log.debug("Registered automatic task: {}", tarea.getTaskKey());
        }
    }

    public void detenerTarea(String taskKey) {
        if (taskKey != null) {
            this.tareaManager.detener(taskKey);
            log.debug("Stopped automatic task: {}", taskKey);
        }
    }

    public void conectar(String websocketUrl, String token) throws ConexionFallidaException {
        if (this.state != ConnectionState.DISCONNECTED) {
            throw new IllegalStateException("Already connected or connecting");
        } else if (websocketUrl != null && !websocketUrl.isBlank()) {
            if (token != null && !token.isBlank()) {
                try {
                    this.state = ConnectionState.CONNECTING;
                    this.loginFuture = new CompletableFuture();
                    URI uri = URI.create(websocketUrl);
                    String scheme = uri.getScheme();
                    if (scheme != null && (scheme.equals("ws") || scheme.equals("wss"))) {
                        log.info("Connecting to {} (secure: {})", uri, scheme.equals("wss"));
                        HttpClient client = HttpClient.newHttpClient();
                        WebSocketHandler handler = new WebSocketHandler(this::onMessageReceived, this::onWebSocketError, this::onWebSocketClosed);
                        this.webSocket = (WebSocket)client.newWebSocketBuilder().connectTimeout(this.config.getConnectionTimeout()).buildAsync(uri, handler).join();
                        this.state = ConnectionState.CONNECTED;
                        log.info("Connected to {}", uri);
                        this.enviarLogin(token);
                        this.startHeartbeat();
                    } else {
                        throw new IllegalArgumentException("URL must start with ws:// or wss://");
                    }
                } catch (Exception e) {
                    this.state = ConnectionState.DISCONNECTED;
                    this.loginFuture = null;
                    throw new ConexionFallidaException("Failed to connect to " + websocketUrl, websocketUrl, 0, e);
                }
            } else {
                throw new IllegalArgumentException("token cannot be null or blank");
            }
        } else {
            throw new IllegalArgumentException("websocketUrl cannot be null or blank");
        }
    }

    public void conectar(String host, int port, String token) throws ConexionFallidaException {
        this.conectarInternal(host, port, token, false);
    }

    public void conectarSeguro(String host, int port, String token) throws ConexionFallidaException {
        this.conectarInternal(host, port, token, true);
    }

    private void conectarInternal(String host, int port, String token, boolean secure) throws ConexionFallidaException {
        if (this.state != ConnectionState.DISCONNECTED) {
            throw new IllegalStateException("Already connected or connecting");
        } else if (host != null && !host.isBlank()) {
            if (port > 0 && port <= 65535) {
                if (token != null && !token.isBlank()) {
                    try {
                        this.state = ConnectionState.CONNECTING;
                        this.loginFuture = new CompletableFuture();
                        String protocol = secure ? "wss" : "ws";
                        URI uri = URI.create(String.format("%s://%s:%d", protocol, host, port));
                        log.info("Connecting to {} (secure: {})", uri, secure);
                        HttpClient client = HttpClient.newHttpClient();
                        WebSocketHandler handler = new WebSocketHandler(this::onMessageReceived, this::onWebSocketError, this::onWebSocketClosed);
                        this.webSocket = (WebSocket)client.newWebSocketBuilder().connectTimeout(this.config.getConnectionTimeout()).buildAsync(uri, handler).join();
                        this.state = ConnectionState.CONNECTED;
                        log.info("Connected to {}", uri);
                        this.enviarLogin(token);
                        this.startHeartbeat();
                    } catch (Exception e) {
                        this.state = ConnectionState.DISCONNECTED;
                        this.loginFuture = null;
                        throw new ConexionFallidaException("Failed to connect to " + host + ":" + port, host, port, e);
                    }
                } else {
                    throw new IllegalArgumentException("token cannot be null or blank");
                }
            } else {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
        } else {
            throw new IllegalArgumentException("host cannot be null or blank");
        }
    }

    public void desconectar() {
        if (this.state != ConnectionState.DISCONNECTED) {
            log.info("Disconnecting...");
            this.stopHeartbeat();
            if (this.webSocket != null) {
                this.webSocket.sendClose(1000, "Client disconnect");
                this.webSocket = null;
            }

            this.state = ConnectionState.DISCONNECTED;
            this.loginFuture = null;
            log.info("Disconnected");
        }
    }

    private void waitForAuthentication() {
        if (this.state != ConnectionState.AUTHENTICATED) {
            if (this.loginFuture == null) {
                throw new IllegalStateException("Not connected");
            } else {
                try {
                    this.loginFuture.get(this.config.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new IllegalStateException("Authentication timed out", e);
                } catch (Exception e) {
                    throw new IllegalStateException("Authentication failed", e);
                }
            }
        }
    }

    public void enviarLogin(String token) {
        if (this.state != ConnectionState.CONNECTED) {
            throw new IllegalStateException("Not connected");
        } else if (token != null && !token.isBlank()) {
            LoginMessage login = LoginMessage.builder().type(MessageType.LOGIN).token(token).tz("UTC").build();
            this.sendMessage(login);
        } else {
            throw new IllegalArgumentException("token cannot be null or blank");
        }
    }

    public void enviarOrden(OrderMessage order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        } else {
            this.waitForAuthentication();
            order.setType(MessageType.ORDER);
            this.sendMessage(order);
        }
    }

    public void enviarCancelacion(String clOrdID) {
        if (clOrdID != null && !clOrdID.isBlank()) {
            this.waitForAuthentication();
            CancelMessage cancel = CancelMessage.builder().type(MessageType.CANCEL).clOrdID(clOrdID).build();
            this.sendMessage(cancel);
        } else {
            throw new IllegalArgumentException("clOrdID cannot be null or blank");
        }
    }

    public void enviarActualizacionProduccion(ProductionUpdateMessage update) {
        if (update == null) {
            throw new IllegalArgumentException("update cannot be null");
        } else {
            this.waitForAuthentication();
            update.setType(MessageType.PRODUCTION_UPDATE);
            this.sendMessage(update);
        }
    }

    public void enviarRespuestaOferta(AcceptOfferMessage response) {
        if (response == null) {
            throw new IllegalArgumentException("response cannot be null");
        } else {
            this.waitForAuthentication();
            response.setType(MessageType.ACCEPT_OFFER);
            this.sendMessage(response);
        }
    }

    private void sendMessage(Object message) {
        if (this.webSocket != null && !this.webSocket.isOutputClosed()) {
            try {
                this.sendLock.acquire();

                try {
                    String json = JsonSerializer.toJson(message);
                    log.debug("Sending: {}", json);
                    this.webSocket.sendText(json, true).join();
                } finally {
                    this.sendLock.release();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Send interrupted", e);
            }
        } else {
            throw new IllegalStateException("WebSocket not connected");
        }
    }

    private void onMessageReceived(String json) {
        log.trace("Received: {}", json);
        this.sequencer.submit(() -> this.router.routeMessage(json, this.createHandlers()));
    }

    private void onWebSocketError(Throwable error) {
        log.error("WebSocket error", error);
        this.state = ConnectionState.DISCONNECTED;
        this.stopHeartbeat();
        this.notifyConnectionLost(error);
    }

    private void onWebSocketClosed() {
        log.info("WebSocket closed");
        this.state = ConnectionState.DISCONNECTED;
        this.stopHeartbeat();
    }

    private MessageRouter.MessageHandlers createHandlers() {
        return new MessageRouter.MessageHandlers() {
            {
                Objects.requireNonNull(ConectorBolsa.this);
            }

            public void onLoginOk(LoginOKMessage message) {
                ConectorBolsa.this.state = ConnectionState.AUTHENTICATED;
                ConectorBolsa.log.info("Authenticated as team: {}", message.getTeam());
                if (ConectorBolsa.this.loginFuture != null) {
                    ConectorBolsa.this.loginFuture.complete(message);
                }

                ConectorBolsa.this.notifyListeners((l) -> l.onLoginOk(message));
            }

            public void onFill(FillMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onFill(message));
            }

            public void onTicker(TickerMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onTicker(message));
            }

            public void onOffer(OfferMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onOffer(message));
            }

            public void onError(ErrorMessage message) {
                if (message.getCode() == ErrorCode.AUTH_FAILED && ConectorBolsa.this.loginFuture != null && !ConectorBolsa.this.loginFuture.isDone()) {
                    ConectorBolsa.this.loginFuture.completeExceptionally(new RuntimeException("Authentication failed: " + message.getReason()));
                }

                ConectorBolsa.this.notifyListeners((l) -> l.onError(message));
            }

            public void onOrderAck(OrderAckMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onOrderAck(message));
            }

            public void onInventoryUpdate(InventoryUpdateMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onInventoryUpdate(message));
            }

            public void onBalanceUpdate(BalanceUpdateMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onBalanceUpdate(message));
            }

            public void onEventDelta(EventDeltaMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onEventDelta(message));
            }

            public void onBroadcast(BroadcastNotificationMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onBroadcast(message));
            }

            public void onPong(PongMessage message) {
                if (ConectorBolsa.this.heartbeatManager != null) {
                    ConectorBolsa.this.heartbeatManager.onPongReceived();
                }

            }

            public void onGlobalPerformanceReport(GlobalPerformanceReportMessage message) {
                ConectorBolsa.this.notifyListeners((l) -> l.onGlobalPerformanceReport(message));
            }
        };
    }

    private void notifyListeners(Consumer<EventListener> action) {
        this.listeners.forEach((listener) -> this.callbackExecutor.execute(() -> {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("Listener error", e);
            }

        }));
    }

    private void notifyConnectionLost(Throwable error) {
        this.listeners.forEach((listener) -> this.callbackExecutor.execute(() -> {
            try {
                listener.onConnectionLost(error);
            } catch (Exception e) {
                log.error("Listener error in onConnectionLost", e);
            }

        }));
    }

    private void startHeartbeat() {
        if (this.heartbeatManager != null) {
            this.heartbeatManager.stop();
        }

        this.heartbeatManager = new HeartbeatManager(this.config.getHeartbeatInterval(), this.config.getHeartbeatInterval().multipliedBy(3L), this::sendMessage, this::onPongTimeout);
        this.heartbeatManager.start();
        log.debug("Heartbeat started");
    }

    private void stopHeartbeat() {
        if (this.heartbeatManager != null) {
            this.heartbeatManager.stop();
            this.heartbeatManager = null;
            log.debug("Heartbeat stopped");
        }

    }

    private void onPongTimeout() {
        log.warn("Pong timeout - disconnecting");
        this.desconectar();
        this.notifyConnectionLost(new RuntimeException("Pong timeout"));
    }

    public void shutdown() {
        this.desconectar();
        this.tareaManager.shutdown();
        this.sequencer.shutdown();
        this.callbackExecutor.shutdown();
        log.info("SDK shutdown complete");
    }

    @Generated
    public ConnectionState getState() {
        return this.state;
    }
}
