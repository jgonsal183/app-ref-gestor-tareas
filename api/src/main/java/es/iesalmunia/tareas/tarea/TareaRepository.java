package es.iesalmunia.tareas.tarea;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /** Busca una tarea solo si pertenece al usuario indicado. */
    Optional<Tarea> findByIdAndPropietarioId(Long id, Long propietarioId);

    /**
     * Tareas de un usuario con filtros opcionales (un filtro nulo no se aplica).
     * El patrón de texto nunca es nulo: si no se filtra por texto, vale "%".
     */
    @Query("""
            select t from Tarea t
            where t.propietarioId = :propietarioId
              and (:estado is null or t.estado = :estado)
              and (:prioridad is null or t.prioridad = :prioridad)
              and lower(t.titulo) like :patron
            """)
    List<Tarea> buscar(@Param("propietarioId") Long propietarioId,
                       @Param("estado") EstadoTarea estado,
                       @Param("prioridad") Prioridad prioridad,
                       @Param("patron") String patron);

    /** Recuento de tareas por estado. Cada fila es [estado, número]. */
    @Query("select t.estado, count(t) from Tarea t where t.propietarioId = :propietarioId group by t.estado")
    List<Object[]> contarPorEstado(@Param("propietarioId") Long propietarioId);

    long countByPropietarioIdAndEstadoNotAndFechaLimiteBefore(Long propietarioId, EstadoTarea estado, LocalDate fecha);
}
