package es.iesalmunia.tareas.correo;

import es.iesalmunia.tareas.info.Instancia;
import es.iesalmunia.tareas.usuario.UsuarioActual;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CorreoController {

    public record PeticionPrueba(
            @NotBlank(message = "Indica el destinatario")
            @Email(message = "El destinatario no es una dirección válida")
            String destinatario) {
    }

    private final CorreoService correo;
    private final UsuarioActual usuarioActual;

    public CorreoController(CorreoService correo, UsuarioActual usuarioActual) {
        this.correo = correo;
        this.usuarioActual = usuarioActual;
    }

    /** POST /api/correo/prueba {"destinatario": "alguien@ejemplo.com"} */
    @PostMapping("/api/correo/prueba")
    public ResponseEntity<Void> prueba(@Valid @RequestBody PeticionPrueba peticion) {
        String texto = "Correo de prueba del gestor de tareas.%n%nEnviado por: %s%nDesde la instancia: %s%n"
                .formatted(usuarioActual.obtener().getNombre(), Instancia.nombre());
        correo.enviar(peticion.destinatario(), "Prueba del gestor de tareas", texto);
        return ResponseEntity.accepted().build();
    }
}
