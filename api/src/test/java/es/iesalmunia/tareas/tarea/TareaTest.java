package es.iesalmunia.tareas.tarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias: no arrancan Spring ni necesitan base de datos, así que
 * "./mvnw package" funciona en cualquier sitio (también dentro de un Dockerfile).
 */
class TareaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    private Tarea tarea(EstadoTarea estado, LocalDate fechaLimite) {
        Tarea tarea = new Tarea(1L);
        tarea.setEstado(estado);
        tarea.setFechaLimite(fechaLimite);
        return tarea;
    }

    @Test
    void pendienteConFechaPasadaEstaVencida() {
        assertThat(tarea(EstadoTarea.PENDIENTE, HOY.minusDays(1)).estaVencida(HOY)).isTrue();
    }

    @Test
    void hechaNuncaEstaVencida() {
        assertThat(tarea(EstadoTarea.HECHA, HOY.minusDays(30)).estaVencida(HOY)).isFalse();
    }

    @Test
    void sinFechaLimiteNoEstaVencida() {
        assertThat(tarea(EstadoTarea.EN_CURSO, null).estaVencida(HOY)).isFalse();
    }

    @Test
    void elMismoDiaNoEstaVencida() {
        assertThat(tarea(EstadoTarea.PENDIENTE, HOY).estaVencida(HOY)).isFalse();
    }
}
