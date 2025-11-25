package tech.hellsoft.trading.util;

import tech.hellsoft.trading.modelo.Rol;

public class CalculadoraProduccion {


     // Calcula la producción total usando recursión.//
    public static int calcularUnidades(Rol rol) {
        if (rol == null) {
            return 0;
        }
        return calcularRecursivo(0, rol);
    }

    //Función recursiva que suma las contribuciones de cada nivel//

    private static int calcularRecursivo(int nivel, Rol rol) {

        // Caso base: profundidad máxima alcanzada
        if (nivel > rol.getMaxDepth()) {
            return 0;
        }

        // Energía en este nivel
        double energia = rol.getBaseEnergy() + rol.getLevelEnergy() * nivel;

        // Factor multiplicador: decay^nivel × branches^nivel
        double decayPow = Math.pow(rol.getDecay(), nivel);
        double branchesPow = Math.pow(rol.getBranches(), nivel);
        double factor = decayPow * branchesPow;

        // Contribución del nivel actual
        int contribucion = (int) Math.round(energia * factor);

        // Recursión: sumar con los niveles siguientes
        return contribucion + calcularRecursivo(nivel + 1, rol);
    }

    //Aplica el bonus premium (ej. 1.30 para +30%)//
    public static int aplicarBonusPremium(int unidadesBase, double bonus) {
        return (int) Math.round(unidadesBase * bonus);
    }
}