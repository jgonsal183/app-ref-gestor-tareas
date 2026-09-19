package es.iesalmunia.tareas.tarea;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Resumen de las tareas de un usuario. Se guarda en la caché, por eso es
 * Serializable: con Redis, el objeto viaja a Redis y vuelve.
 *
 * "generadoPor" y "generadoEn" permiten ver de dónde sale el dato: si varias
 * instancias comparten Redis, una puede servir estadísticas calculadas por otra.
 */
public record EstadisticasTareas(
        long total,
        Map<EstadoTarea, Long> porEstado,
        long vencidas,
        String generadoPor,
        Instant generadoEn) implements Serializable {
}
