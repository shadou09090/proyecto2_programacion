package tech.hellsoft.trading;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class TareaAutomatica {

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  protected abstract void ejecutar();

  public void iniciar(int intervaloSegundos) {
    scheduler.scheduleAtFixedRate(this::ejecutar, intervaloSegundos, intervaloSegundos, TimeUnit.SECONDS);
  }

  public void detener() {
    scheduler.shutdownNow();
  }
}
