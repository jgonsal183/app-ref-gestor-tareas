package es.iesalmunia.tareas.adjunto;

import es.iesalmunia.tareas.almacen.AlmacenAdjuntos;
import es.iesalmunia.tareas.error.RecursoNoEncontradoException;
import es.iesalmunia.tareas.tarea.TareaRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Gestión de adjuntos. Antes de cualquier operación se comprueba que la tarea
 * pertenece al usuario que hace la petición.
 */
@Service
@Transactional
public class AdjuntoService {

    private static final Logger log = LoggerFactory.getLogger(AdjuntoService.class);

    /** Un adjunto junto con su contenido, para descargarlo. */
    public record Descarga(Adjunto adjunto, InputStream contenido) {
    }

    private final AdjuntoRepository adjuntos;
    private final TareaRepository tareas;
    private final AlmacenAdjuntos almacen;

    public AdjuntoService(AdjuntoRepository adjuntos, TareaRepository tareas, AlmacenAdjuntos almacen) {
        this.adjuntos = adjuntos;
        this.tareas = tareas;
        this.almacen = almacen;
    }

    @Transactional(readOnly = true)
    public List<Adjunto> listar(Long propietarioId, Long tareaId) {
        comprobarTarea(propietarioId, tareaId);
        return adjuntos.findByTareaIdOrderBySubidoEn(tareaId);
    }

    public Adjunto subir(Long propietarioId, Long tareaId, MultipartFile fichero) throws IOException {
        comprobarTarea(propietarioId, tareaId);
        String clave = UUID.randomUUID().toString();
        String nombre = fichero.getOriginalFilename() != null ? fichero.getOriginalFilename() : "fichero";
        try (InputStream datos = fichero.getInputStream()) {
            almacen.guardar(clave, datos, fichero.getSize(), fichero.getContentType());
        }
        return adjuntos.save(new Adjunto(tareaId, nombre, fichero.getContentType(), fichero.getSize(), clave));
    }

    @Transactional(readOnly = true)
    public Descarga descargar(Long propietarioId, Long adjuntoId) throws IOException {
        Adjunto adjunto = obtener(propietarioId, adjuntoId);
        return new Descarga(adjunto, almacen.abrir(adjunto.getClave()));
    }

    public void borrar(Long propietarioId, Long adjuntoId) throws IOException {
        Adjunto adjunto = obtener(propietarioId, adjuntoId);
        almacen.borrar(adjunto.getClave());
        adjuntos.delete(adjunto);
    }

    /** Se usa al borrar una tarea. La comprobación de propietario ya la hizo TareaService. */
    public void borrarTodosDeTarea(Long tareaId) {
        for (Adjunto adjunto : adjuntos.findByTareaIdOrderBySubidoEn(tareaId)) {
            try {
                almacen.borrar(adjunto.getClave());
            } catch (IOException | UncheckedIOException e) {
                log.warn("No se pudo borrar el fichero {} del almacén: {}", adjunto.getClave(), e.getMessage());
            }
            adjuntos.delete(adjunto);
        }
    }

    private Adjunto obtener(Long propietarioId, Long adjuntoId) {
        Adjunto adjunto = adjuntos.findById(adjuntoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el adjunto " + adjuntoId));
        comprobarTarea(propietarioId, adjunto.getTareaId());
        return adjunto;
    }

    private void comprobarTarea(Long propietarioId, Long tareaId) {
        if (tareas.findByIdAndPropietarioId(tareaId, propietarioId).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe la tarea " + tareaId);
        }
    }
}
