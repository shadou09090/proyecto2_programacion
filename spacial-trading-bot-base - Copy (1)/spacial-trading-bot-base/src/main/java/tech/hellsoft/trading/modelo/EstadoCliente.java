package tech.hellsoft.trading.modelo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class EstadoCliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private double saldo;
    private double saldoInicial;
    private Map<String, Integer> inventario;
    private Map<String, Double> preciosActuales;
    private Map<String, Receta> recetas;
    private Rol rol;
    private List<String> productosAutorizados;

    public EstadoCliente() {
        this.saldo = 0.0;
        this.saldoInicial = 0.0;
        this.inventario = new HashMap<>();
        this.preciosActuales = new HashMap<>();
        this.recetas = new HashMap<>();
        this.productosAutorizados = new ArrayList<>();
    }

    public double calcularPL() {
        double valorInventario = 0.0;

        for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
            String producto = entry.getKey();
            int cantidad = entry.getValue();
            double precio = preciosActuales.getOrDefault(producto, 0.0);
            valorInventario += cantidad * precio;
        }

        double patrimonioNeto = saldo + valorInventario;
        if (saldoInicial == 0) return 0.0;

        return ((patrimonioNeto - saldoInicial) / saldoInicial) * 100.0;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}

