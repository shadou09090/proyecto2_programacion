package tech.hellsoft.trading.eventos;

import tech.hellsoft.trading.dto.server.BalanceUpdateMessage;
import tech.hellsoft.trading.dto.server.BroadcastNotificationMessage;
import tech.hellsoft.trading.dto.server.ErrorMessage;
import tech.hellsoft.trading.dto.server.EventDeltaMessage;
import tech.hellsoft.trading.dto.server.FillMessage;
import tech.hellsoft.trading.dto.server.InventoryUpdateMessage;
import tech.hellsoft.trading.dto.server.LoginOKMessage;
import tech.hellsoft.trading.dto.server.OfferMessage;
import tech.hellsoft.trading.dto.server.OrderAckMessage;
import tech.hellsoft.trading.dto.server.TickerMessage;

public interface EventListener {

  void onLoginOk(LoginOKMessage loginOk);

  void onError(ErrorMessage error);

  void onTicker(TickerMessage ticker);

  void onFill(FillMessage fill);

  void onBalanceUpdate(BalanceUpdateMessage balanceUpdate);

  void onInventoryUpdate(InventoryUpdateMessage inventoryUpdate);

  void onOffer(OfferMessage offer);

  void onOrderAck(OrderAckMessage orderAck);

  void onEventDelta(EventDeltaMessage eventDelta);

  void onBroadcast(BroadcastNotificationMessage broadcast);

  void onConnectionLost(Throwable throwable);


}
