package tech.hellsoft.trading.util;

import tech.hellsoft.trading.modelo.Rol;
public class CalculadoraProduccion {
    /**
     * Calcula las unidades producidas usando recursión.
     */
    public static int calcularUnidades(Rol rol) {
        return calcularRecursivo(0, rol);
    }

    /**
     * Función recursiva que suma contribuciones de cada nivel.
     */
    private static int calcularRecursivo(int nivel, Rol rol) {
        // ⚠ CASO BASE: Profundidad máxima alcanzada
        if (nivel > rol.getMaxDepth()) {
            return 0;
        }
        // Calcular energía en este nivel
        double energia = rol.getBaseEnergy() + rol.getLevelEnergy() * nivel;
        // Calcular factor multiplicador
        double decay = Math.pow(rol.getDecay(), nivel);
        double branches = Math.pow(rol.getBranches(), nivel);
        double factor = decay * branches;
        // Contribución de este nivel
        int contribucion = (int) Math.round(energia * factor);
        // 🔄 CASO RECURSIVO: Sumar contribuciones de niveles inferiores
        return contribucion + calcularRecursivo(nivel + 1, rol);
    }

    /**
     * Aplica el bonus de producción premium (+30%).
     */
    public static int aplicarBonusPremium(int unidadesBase, double bonus) {
        return (int) Math.round(unidadesBase * bonus);
    }
}