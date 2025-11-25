package tech.hellsoft.trading.util;

import tech.hellsoft.trading.Cliente.EstadoCliente;
import tech.hellsoft.trading.exception.ConfiguracionException.SnapshotCorruptoException;

import java.io.*;

public final class SnapshotManager {

    private SnapshotManager() {
    }

    //Guarda el estado completo del cliente en un archivo binario//
    public static void guardar(EstadoCliente estado, String archivo)
            throws SnapshotCorruptoException {

        if (estado == null) {
            throw new SnapshotCorruptoException("Estado nulo, no se puede guardar.");
        }

        if (archivo == null || archivo.isBlank()) {
            throw new SnapshotCorruptoException("Ruta inválida al guardar snapshot.");
        }

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(archivo))) {

            out.writeObject(estado);

        } catch (IOException e) {
            throw new SnapshotCorruptoException(
                    "Error al guardar snapshot: " + e.getMessage(), e
            );
        }
    }

    // Carga un snapshot desde un archivo binario//
    public static EstadoCliente cargar(String archivo)
            throws SnapshotCorruptoException {

        if (archivo == null || archivo.isBlank()) {
            throw new SnapshotCorruptoException("Ruta inválida al cargar snapshot.");
        }

        File file = new File(archivo);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new SnapshotCorruptoException("Archivo de snapshot no válido: " + archivo);
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {

            Object obj = in.readObject();
            if (!(obj instanceof EstadoCliente estado)) {
                throw new SnapshotCorruptoException("Formato de snapshot inválido.");
            }

            return estado;

        } catch (IOException | ClassNotFoundException e) {
            throw new SnapshotCorruptoException(
                    "Error al cargar snapshot: " + e.getMessage(), e
            );
        }
    }
}
