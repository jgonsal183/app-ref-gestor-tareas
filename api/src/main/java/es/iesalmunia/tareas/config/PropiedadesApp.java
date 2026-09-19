package es.iesalmunia.tareas.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades propias de la aplicación (bloque "app" de application.yaml).
 * Cada valor procede de una variable de entorno; ver el README.
 */
@ConfigurationProperties(prefix = "app")
public record PropiedadesApp(
        boolean login,
        String demoPassword,
        String corsOrigins,
        Almacenamiento almacenamiento,
        Redis redis,
        Correo correo) {

    public record Almacenamiento(String tipo, String directorio, String bucket, String region) {
    }

    public record Redis(String host, int port, String password) {
    }

    public record Correo(String host, int port, String remitente) {
    }

    /** Orígenes CORS como lista, ignorando los valores vacíos. */
    public List<String> listaCorsOrigins() {
        if (corsOrigins == null || corsOrigins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();
    }

    public boolean redisActivo() {
        return redis != null && redis.host() != null && !redis.host().isBlank();
    }

    public boolean correoActivo() {
        return correo != null && correo.host() != null && !correo.host().isBlank();
    }
}
