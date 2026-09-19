package es.iesalmunia.tareas.usuario;

import es.iesalmunia.tareas.config.PropiedadesApp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Indica quién hace la petición actual.
 * Con LOGIN=false siempre es el usuario "invitado".
 */
@Component
public class UsuarioActual {

    private final UsuarioRepository usuarios;
    private final PropiedadesApp propiedades;

    public UsuarioActual(UsuarioRepository usuarios, PropiedadesApp propiedades) {
        this.usuarios = usuarios;
        this.propiedades = propiedades;
    }

    public Usuario obtener() {
        String nombreUsuario = Usuario.INVITADO;
        if (propiedades.login()) {
            Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
            nombreUsuario = autenticacion.getName();
        }
        String buscado = nombreUsuario;
        return usuarios.findByNombreUsuario(buscado)
                .orElseThrow(() -> new IllegalStateException("No existe el usuario " + buscado));
    }

    public Long id() {
        return obtener().getId();
    }
}
