package es.iesalmunia.tareas.usuario;

import es.iesalmunia.tareas.config.PropiedadesApp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El login y el logout los atiende Spring Security (ver SeguridadConfig):
 *   POST /api/auth/login   (campos de formulario: usuario, contrasena)
 *   POST /api/auth/logout
 * Aquí solo se responde a "¿quién soy?", que el frontend consulta al cargar.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record Yo(boolean loginActivo, String usuario, String nombre) {
    }

    private final UsuarioActual usuarioActual;
    private final PropiedadesApp propiedades;

    public AuthController(UsuarioActual usuarioActual, PropiedadesApp propiedades) {
        this.usuarioActual = usuarioActual;
        this.propiedades = propiedades;
    }

    @GetMapping("/yo")
    public ResponseEntity<Yo> yo() {
        if (propiedades.login() && !haySesionIniciada()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuario = usuarioActual.obtener();
        return ResponseEntity.ok(new Yo(propiedades.login(), usuario.getNombreUsuario(), usuario.getNombre()));
    }

    private boolean haySesionIniciada() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null
                && autenticacion.isAuthenticated()
                && !(autenticacion instanceof AnonymousAuthenticationToken);
    }
}
