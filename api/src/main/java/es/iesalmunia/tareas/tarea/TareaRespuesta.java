package es.iesalmunia.tareas.tarea;

import java.time.Instant;
import java.time.LocalDate;

/** Lo que la API devuelve de cada tarea. */
public record TareaRespuesta(
        Long id,
        String titulo,
        String descripcion,
        EstadoTarea estado,
        Prioridad prioridad,
        LocalDate fechaLimite,
        boolean vencida,
        Instant creadaEn,
        Instant actualizadaEn) {

    public static TareaRespuesta de(Tarea tarea) {
        return new TareaRespuesta(
                tarea.getId(),
                tarea.getTitulo(),
                tarea.getDescripcion(),
                tarea.getEstado(),
                tarea.getPrioridad(),
                tarea.getFechaLimite(),
                tarea.estaVencida(LocalDate.now()),
                tarea.getCreadaEn(),
                tarea.getActualizadaEn());
    }
}
