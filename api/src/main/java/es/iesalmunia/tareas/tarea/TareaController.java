package es.iesalmunia.tareas.tarea;

import es.iesalmunia.tareas.usuario.UsuarioActual;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService servicio;
    private final UsuarioActual usuarioActual;

    public TareaController(TareaService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    /** GET /api/tareas?estado=PENDIENTE&prioridad=ALTA&texto=docker (filtros opcionales) */
    @GetMapping
    public List<TareaRespuesta> listar(@RequestParam(required = false) EstadoTarea estado,
                                       @RequestParam(required = false) Prioridad prioridad,
                                       @RequestParam(required = false) String texto) {
        return servicio.listar(usuarioActual.id(), estado, prioridad, texto).stream()
                .map(TareaRespuesta::de)
                .toList();
    }

    @GetMapping("/estadisticas")
    public EstadisticasTareas estadisticas() {
        return servicio.estadisticas(usuarioActual.id());
    }

    @GetMapping("/{id}")
    public TareaRespuesta obtener(@PathVariable Long id) {
        return TareaRespuesta.de(servicio.obtener(usuarioActual.id(), id));
    }

    @PostMapping
    public ResponseEntity<TareaRespuesta> crear(@Valid @RequestBody TareaPeticion peticion) {
        Tarea tarea = servicio.crear(usuarioActual.id(), peticion);
        return ResponseEntity.created(URI.create("/api/tareas/" + tarea.getId()))
                .body(TareaRespuesta.de(tarea));
    }

    @PutMapping("/{id}")
    public TareaRespuesta actualizar(@PathVariable Long id, @Valid @RequestBody TareaPeticion peticion) {
        return TareaRespuesta.de(servicio.actualizar(usuarioActual.id(), id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public TareaRespuesta cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstado cambio) {
        return TareaRespuesta.de(servicio.cambiarEstado(usuarioActual.id(), id, cambio.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        servicio.borrar(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
