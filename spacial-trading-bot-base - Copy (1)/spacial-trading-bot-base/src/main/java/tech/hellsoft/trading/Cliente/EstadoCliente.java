import tech.hellsoft.trading.modelo.Receta;
import tech.hellsoft.trading.modelo.Rol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return ((patrimonioNeto - saldoInicial) / saldoInicial) * 100.0;
    }
// Getters y setters para todos los campos...
}