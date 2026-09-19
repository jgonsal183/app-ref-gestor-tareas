package es.iesalmunia.tareas.info;

import es.iesalmunia.tareas.almacen.AlmacenAdjuntos;
import es.iesalmunia.tareas.config.PropiedadesApp;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/info: cómo está desplegada esta instancia de la API.
 * No requiere login y no muestra contraseñas ni datos sensibles.
 */
@RestController
public class InfoController {

    public record Info(
            String aplicacion,
            String version,
            String instancia,
            Instant arrancadaEn,
            String java,
            String baseDatos,
            boolean login,
            String sesiones,
            String cache,
            String almacenamiento,
            String correo) {
    }

    private static final Instant ARRANQUE = Instant.now();

    private final PropiedadesApp propiedades;
    private final ObjectProvider<BuildProperties> construccion;
    private final DataSource dataSource;
    private final CacheManager cacheManager;
    private final AlmacenAdjuntos almacen;
    private String baseDatos; // se calcula la primera vez

    public InfoController(PropiedadesApp propiedades, ObjectProvider<BuildProperties> construccion,
                          DataSource dataSource, CacheManager cacheManager, AlmacenAdjuntos almacen) {
        this.propiedades = propiedades;
        this.construccion = construccion;
        this.dataSource = dataSource;
        this.cacheManager = cacheManager;
        this.almacen = almacen;
    }

    @GetMapping("/api/info")
    public Info info() {
        BuildProperties build = construccion.getIfAvailable();
        String version = build != null ? build.getVersion() : "desarrollo";

        String sesiones;
        if (!propiedades.login()) {
            sesiones = "sin sesiones";
        } else {
            sesiones = propiedades.redisActivo() ? "redis" : "memoria";
        }
        String cache = cacheManager instanceof RedisCacheManager ? "redis" : "memoria";
        String correo = propiedades.correoActivo()
                ? propiedades.correo().host() + ":" + propiedades.correo().port()
                : "desactivado";

        return new Info("gestor-tareas", version, Instancia.nombre(), ARRANQUE,
                System.getProperty("java.version"), baseDatos(), propiedades.login(),
                sesiones, cache, almacen.descripcion(), correo);
    }

    private synchronized String baseDatos() {
        if (baseDatos == null) {
            try (Connection conexion = dataSource.getConnection()) {
                DatabaseMetaData metadatos = conexion.getMetaData();
                baseDatos = metadatos.getDatabaseProductName() + " " + metadatos.getDatabaseProductVersion();
            } catch (SQLException e) {
                return "no disponible (" + e.getMessage() + ")";
            }
        }
        return baseDatos;
    }
}
