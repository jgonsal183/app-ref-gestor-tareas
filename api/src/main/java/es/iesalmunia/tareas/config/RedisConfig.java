package es.iesalmunia.tareas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Conexión con Redis. Solo existe si la variable REDIS_HOST tiene valor.
 *
 * Cuando existe esta conexión, Spring Boot la aprovecha automáticamente para:
 *   - la caché (@Cacheable), en lugar de la caché en memoria, y
 *   - las sesiones de usuario (Spring Session), en lugar de la memoria del servidor.
 * Así, varias instancias de la API comparten caché y sesiones.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("'${app.redis.host:}' != ''")
public class RedisConfig {

    @Bean
    LettuceConnectionFactory redisConnectionFactory(PropiedadesApp propiedades) {
        PropiedadesApp.Redis redis = propiedades.redis();
        RedisStandaloneConfiguration configuracion =
                new RedisStandaloneConfiguration(redis.host(), redis.port());
        if (redis.password() != null && !redis.password().isBlank()) {
            configuracion.setPassword(redis.password());
        }
        return new LettuceConnectionFactory(configuracion);
    }
}
