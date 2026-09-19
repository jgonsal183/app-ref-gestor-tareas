package es.iesalmunia.tareas.tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Datos que envía el cliente para crear o modificar una tarea.
 * Si no se indican estado o prioridad, se usan PENDIENTE y MEDIA.
 */
public record TareaPeticion(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String titulo,

        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        EstadoTarea estado,
        Prioridad prioridad,
        LocalDate fechaLimite) {
}
