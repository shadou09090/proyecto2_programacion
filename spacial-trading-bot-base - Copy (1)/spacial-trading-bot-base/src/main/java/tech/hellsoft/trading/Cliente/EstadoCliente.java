package tech.hellsoft.trading.Cliente;

import tech.hellsoft.trading.modelo.Receta;
import tech.hellsoft.trading.modelo.Rol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadoCliente implements Serializable {

  private static final long serialVersionUID = 1L;

  private double saldo;
  private double saldoInicial;
  private final Map<String, Integer> inventario = new HashMap<>();
  private final Map<String, Double> preciosActuales = new HashMap<>();
  private final Map<String, Receta> recetas = new HashMap<>();
  private Rol rol;
  private final List<String> productosAutorizados = new ArrayList<>();

  public double calcularPL() {
    double valorInventario = 0.0;
    for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
      double precio = preciosActuales.getOrDefault(entry.getKey(), 0.0);
      valorInventario += entry.getValue() * precio;
    }
    double patrimonioNeto = saldo + valorInventario;
    System.out.println("Patrimonio Neto: " + patrimonioNeto);
    return ((patrimonioNeto - saldoInicial) / saldoInicial) * 100.0;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public double getSaldoInicial() {
    return saldoInicial;
  }

  public void setSaldoInicial(double saldoInicial) {
    this.saldoInicial = saldoInicial;
  }

  public Map<String, Integer> getInventario() {
    return inventario;
  }

  public Map<String, Double> getPreciosActuales() {
    return preciosActuales;
  }

  public Map<String, Receta> getRecetas() {
    return recetas;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public List<String> getProductosAutorizados() {
    return productosAutorizados;
  }

  public void setProductosAutorizados(List<String> productosAutorizados) {
    this.productosAutorizados.clear();
    if (productosAutorizados == null) {
      return;
    }
    this.productosAutorizados.addAll(productosAutorizados);
  }
    public void copiarDesde(EstadoCliente otro) {
        this.saldo = otro.saldo;
        this.saldoInicial = otro.saldoInicial;

        this.inventario.clear();
        this.inventario.putAll(otro.inventario);

        this.preciosActuales.clear();
        this.preciosActuales.putAll(otro.preciosActuales);

        this.recetas.clear();
        this.recetas.putAll(otro.recetas);

        this.productosAutorizados.clear();
        this.productosAutorizados.addAll(otro.productosAutorizados);

        this.rol = otro.rol;
    }

}
