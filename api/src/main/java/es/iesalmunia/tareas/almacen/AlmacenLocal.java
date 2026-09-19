package es.iesalmunia.tareas.almacen;

import es.iesalmunia.tareas.config.PropiedadesApp;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Adjuntos en un directorio del servidor (STORAGE=local, opción por defecto). */
@Component
@ConditionalOnProperty(name = "app.almacenamiento.tipo", havingValue = "local", matchIfMissing = true)
public class AlmacenLocal implements AlmacenAdjuntos {

    private static final Logger log = LoggerFactory.getLogger(AlmacenLocal.class);

    private final Path directorio;

    public AlmacenLocal(PropiedadesApp propiedades) throws IOException {
        this.directorio = Path.of(propiedades.almacenamiento().directorio()).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
        log.info("Adjuntos en el directorio local {}", directorio);
    }

    @Override
    public void guardar(String clave, InputStream datos, long tamano, String tipoContenido) throws IOException {
        Files.copy(datos, ruta(clave), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream abrir(String clave) throws IOException {
        return Files.newInputStream(ruta(clave));
    }

    @Override
    public void borrar(String clave) throws IOException {
        Files.deleteIfExists(ruta(clave));
    }

    @Override
    public String descripcion() {
        return "local (" + directorio + ")";
    }

    private Path ruta(String clave) {
        return directorio.resolve(clave);
    }
}
