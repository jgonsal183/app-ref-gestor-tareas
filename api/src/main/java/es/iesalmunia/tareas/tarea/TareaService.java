package es.iesalmunia.tareas.tarea;

import es.iesalmunia.tareas.adjunto.AdjuntoService;
import es.iesalmunia.tareas.error.RecursoNoEncontradoException;
import es.iesalmunia.tareas.info.Instancia;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de las tareas. Cada usuario solo ve y modifica sus propias tareas.
 *
 * Las estadísticas se guardan en la caché "estadisticas" y se descartan
 * cada vez que una tarea cambia.
 */
@Service
@Transactional
public class TareaService {

    static final String CACHE_ESTADISTICAS = "estadisticas";

    /** Primero las que tienen fecha límite más cercana; a igual fecha, las de mayor prioridad. */
    private static final Comparator<Tarea> ORDEN = Comparator
            .comparing(Tarea::getFechaLimite, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Tarea::getPrioridad, Comparator.reverseOrder())
            .thenComparing(Tarea::getId);

    private final TareaRepository tareas;
    private final AdjuntoService adjuntos;

    public TareaService(TareaRepository tareas, AdjuntoService adjuntos) {
        this.tareas = tareas;
        this.adjuntos = adjuntos;
    }

    @Transactional(readOnly = true)
    public List<Tarea> listar(Long propietarioId, EstadoTarea estado, Prioridad prioridad, String texto) {
        String patron = (texto == null || texto.isBlank()) ? "%" : "%" + texto.trim().toLowerCase() + "%";
        return tareas.buscar(propietarioId, estado, prioridad, patron).stream()
                .sorted(ORDEN)
                .toList();
    }

    @Transactional(readOnly = true)
    public Tarea obtener(Long propietarioId, Long id) {
        return tareas.findByIdAndPropietarioId(id, propietarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la tarea " + id));
    }

    @CacheEvict(cacheNames = CACHE_ESTADISTICAS, allEntries = true)
    public Tarea crear(Long propietarioId, TareaPeticion peticion) {
        Tarea tarea = new Tarea(propietarioId);
        copiarDatos(peticion, tarea);
        return tareas.save(tarea);
    }

    @CacheEvict(cacheNames = CACHE_ESTADISTICAS, allEntries = true)
    public Tarea actualizar(Long propietarioId, Long id, TareaPeticion peticion) {
        Tarea tarea = obtener(propietarioId, id);
        copiarDatos(peticion, tarea);
        return tarea; // al terminar la transacción, JPA guarda los cambios
    }

    @CacheEvict(cacheNames = CACHE_ESTADISTICAS, allEntries = true)
    public Tarea cambiarEstado(Long propietarioId, Long id, EstadoTarea estado) {
        Tarea tarea = obtener(propietarioId, id);
        tarea.setEstado(estado);
        return tarea;
    }

    @CacheEvict(cacheNames = CACHE_ESTADISTICAS, allEntries = true)
    public void borrar(Long propietarioId, Long id) {
        Tarea tarea = obtener(propietarioId, id);
        adjuntos.borrarTodosDeTarea(tarea.getId());
        tareas.delete(tarea);
    }

    @Cacheable(cacheNames = CACHE_ESTADISTICAS, key = "#propietarioId")
    @Transactional(readOnly = true)
    public EstadisticasTareas estadisticas(Long propietarioId) {
        Map<EstadoTarea, Long> porEstado = new EnumMap<>(EstadoTarea.class);
        for (EstadoTarea estado : EstadoTarea.values()) {
            porEstado.put(estado, 0L);
        }
        for (Object[] fila : tareas.contarPorEstado(propietarioId)) {
            porEstado.put((EstadoTarea) fila[0], (Long) fila[1]);
        }
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        long vencidas = tareas.countByPropietarioIdAndEstadoNotAndFechaLimiteBefore(
                propietarioId, EstadoTarea.HECHA, LocalDate.now());
        return new EstadisticasTareas(total, porEstado, vencidas, Instancia.nombre(), Instant.now());
    }

    private void copiarDatos(TareaPeticion peticion, Tarea tarea) {
        tarea.setTitulo(peticion.titulo().trim());
        tarea.setDescripcion(peticion.descripcion());
        tarea.setEstado(peticion.estado() != null ? peticion.estado() : EstadoTarea.PENDIENTE);
        tarea.setPrioridad(peticion.prioridad() != null ? peticion.prioridad() : Prioridad.MEDIA);
        tarea.setFechaLimite(peticion.fechaLimite());
    }
}
