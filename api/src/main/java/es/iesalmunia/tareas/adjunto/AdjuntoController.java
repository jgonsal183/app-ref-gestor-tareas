package es.iesalmunia.tareas.adjunto;

import es.iesalmunia.tareas.usuario.UsuarioActual;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AdjuntoController {

    private final AdjuntoService servicio;
    private final UsuarioActual usuarioActual;

    public AdjuntoController(AdjuntoService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/api/tareas/{tareaId}/adjuntos")
    public List<AdjuntoRespuesta> listar(@PathVariable Long tareaId) {
        return servicio.listar(usuarioActual.id(), tareaId).stream()
                .map(AdjuntoRespuesta::de)
                .toList();
    }

    /** Subida con multipart/form-data, en un campo llamado "fichero". */
    @PostMapping("/api/tareas/{tareaId}/adjuntos")
    public ResponseEntity<AdjuntoRespuesta> subir(@PathVariable Long tareaId,
                                                  @RequestParam("fichero") MultipartFile fichero) throws IOException {
        Adjunto adjunto = servicio.subir(usuarioActual.id(), tareaId, fichero);
        return ResponseEntity.created(URI.create("/api/adjuntos/" + adjunto.getId()))
                .body(AdjuntoRespuesta.de(adjunto));
    }

    @GetMapping("/api/adjuntos/{id}")
    public ResponseEntity<InputStreamResource> descargar(@PathVariable Long id) throws IOException {
        AdjuntoService.Descarga descarga = servicio.descargar(usuarioActual.id(), id);
        Adjunto adjunto = descarga.adjunto();
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(adjunto.getNombre(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(tipoDeContenido(adjunto))
                .contentLength(adjunto.getTamano())
                .body(new InputStreamResource(descarga.contenido()));
    }

    /** Si el tipo guardado no es válido, se descarga como binario genérico. */
    private MediaType tipoDeContenido(Adjunto adjunto) {
        try {
            return MediaType.parseMediaType(adjunto.getTipoContenido());
        } catch (IllegalArgumentException e) { // también cubre InvalidMediaTypeException
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @DeleteMapping("/api/adjuntos/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) throws IOException {
        servicio.borrar(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
