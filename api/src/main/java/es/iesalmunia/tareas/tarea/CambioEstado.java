package es.iesalmunia.tareas.tarea;

import jakarta.validation.constraints.NotNull;

public record CambioEstado(@NotNull(message = "El estado es obligatorio") EstadoTarea estado) {
}
