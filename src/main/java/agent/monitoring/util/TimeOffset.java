package agent.monitoring.util;

import java.time.Instant;

// Clase para guardar el desfase entre la hora del Agente y la hora del Servidor
public class TimeOffset {
    // Offset en milisegundos. Es la cantidad que debemos sumar a la hora local para llegar a la hora del servidor.
    private static volatile long offsetMillis = 0;

    public static void setOffset(long offset) {
        offsetMillis = offset;
    }

    // Este es el método que usará el Sampler para obtener la hora consistente.
    public static Instant getSynchronizedTime() {
        return Instant.now().plusMillis(offsetMillis);
    }
}