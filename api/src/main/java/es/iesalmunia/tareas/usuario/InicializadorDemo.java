package es.iesalmunia.tareas.usuario;

import es.iesalmunia.tareas.config.PropiedadesApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Si se define DEMO_PASSWORD, al arrancar se cambia la contraseña de todos los
 * usuarios de ejemplo. Así, en un servidor accesible desde Internet no quedan
 * las contraseñas conocidas que vienen en las migraciones.
 */
@Component
public class InicializadorDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDemo.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final PropiedadesApp propiedades;

    public InicializadorDemo(UsuarioRepository usuarios, PasswordEncoder codificador, PropiedadesApp propiedades) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        String nueva = propiedades.demoPassword();
        if (nueva == null || nueva.isBlank()) {
            return;
        }
        String cifrada = codificador.encode(nueva);
        for (Usuario usuario : usuarios.findByActivoTrue()) {
            usuario.setContrasena(cifrada);
        }
        log.info("Contraseña de los usuarios de ejemplo cambiada con DEMO_PASSWORD");
    }
}
