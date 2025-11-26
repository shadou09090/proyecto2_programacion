package tech.hellsoft.trading.Cliente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tech.hellsoft.trading.dto.server.TickerMessage;
import tech.hellsoft.trading.modelo.Receta;
import tech.hellsoft.trading.modelo.Rol;

public class EstadoCliente implements Serializable {
    private double saldo;
    private double saldoInicial;
    private Map<String, Integer> inventario = new HashMap<>();
    private Map<String, Double> preciosActuales = new HashMap<>();
    private Map<String, Receta> recetas = new HashMap<>();
    private Rol rol;
    private List<String> productosAutorizados = new ArrayList<>();
    public double calcularPL() {
        double valorInventario = 0.0;
        for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
            double precio = preciosActuales.getOrDefault(entry.getKey(), 0.0);
            valorInventario += entry.getValue() * precio;
        }
        double patrimonioNeto = saldo + valorInventario;
        return ((patrimonioNeto- saldoInicial) / saldoInicial) * 100.0;
    }
    // Getters y setters para todos los campos...


    public EstadoCliente(List<String> productosAutorizados, Rol rol, Map<String, Receta> recetas, Map<String, Double> preciosActuales, Map<String, Integer> inventario, double saldoInicial, double saldo) {
        this.productosAutorizados = productosAutorizados;
        this.rol = rol;
        this.recetas = recetas;
        this.preciosActuales = preciosActuales;
        this.inventario = inventario;
        this.saldoInicial = saldoInicial;
        this.saldo = saldo;
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

    public void setInventario(Map<String, Integer> inventario) {
        this.inventario = inventario;
    }

    public Map<String, Double> getPreciosActuales() {
        return preciosActuales;
    }

    public void setPreciosActuales(Map<String, Double> preciosActuales) {
        this.preciosActuales = preciosActuales;
    }

    public Map<String, Receta> getRecetas() {
        return recetas;
    }

    public void setRecetas(Map<String, Receta> recetas) {
        this.recetas = recetas;
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
        this.productosAutorizados = productosAutorizados;
    }
}
