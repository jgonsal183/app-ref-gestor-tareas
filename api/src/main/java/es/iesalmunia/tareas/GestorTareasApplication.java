package es.iesalmunia.tareas;

import es.iesalmunia.tareas.config.PropiedadesApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Punto de entrada de la aplicación.
 */
@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(PropiedadesApp.class)
public class GestorTareasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestorTareasApplication.class, args);
    }
}
