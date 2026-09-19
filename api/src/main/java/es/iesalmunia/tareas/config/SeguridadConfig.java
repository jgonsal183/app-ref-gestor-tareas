package es.iesalmunia.tareas.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API.
 *
 * LOGIN=false (por defecto): todas las peticiones se permiten y no se crean
 * sesiones. Todo el mundo trabaja como el usuario "invitado".
 *
 * LOGIN=true: hay que iniciar sesión con POST /api/auth/login. La sesión se
 * guarda en la memoria de la instancia o, si hay REDIS_HOST, en Redis.
 *
 * Nota sobre CSRF: está desactivado para simplificar el frontend. La cookie de
 * sesión es SameSite=Strict, de modo que el navegador no la envía en peticiones
 * iniciadas desde otros sitios, que es el ataque que CSRF pretende evitar.
 */
@Configuration(proxyBeanMethods = false)
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, PropiedadesApp propiedades) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        // Sin sesión iniciada se responde 401 (sin redirigir a ninguna página de login)
        http.exceptionHandling(excepciones -> excepciones
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        if (!propiedades.login()) {
            http.authorizeHttpRequests(peticiones -> peticiones.anyRequest().permitAll());
            http.sessionManagement(sesiones -> sesiones.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }

        http.authorizeHttpRequests(peticiones -> peticiones
                .requestMatchers("/actuator/health/**", "/actuator/info", "/api/info", "/api/auth/**", "/error")
                .permitAll()
                .anyRequest().authenticated());

        // Formulario de login: POST /api/auth/login con los campos "usuario" y "contrasena"
        http.formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("usuario")
                .passwordParameter("contrasena")
                .successHandler((peticion, respuesta, autenticacion) ->
                        respuesta.setStatus(HttpStatus.NO_CONTENT.value()))
                .failureHandler((peticion, respuesta, error) ->
                        respuesta.setStatus(HttpStatus.UNAUTHORIZED.value())));

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));

        return http.build();
    }

    /** Las contraseñas se guardan cifradas con BCrypt: {bcrypt}$2a$10$... */
    @Bean
    PasswordEncoder codificadorDeContrasenas() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * CORS: solo hace falta si el frontend se sirve desde otro origen
     * (por ejemplo, un bucket S3). Si CORS_ORIGINS está vacío, no se añade nada.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(PropiedadesApp propiedades) {
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        List<String> origenes = propiedades.listaCorsOrigins();
        if (!origenes.isEmpty()) {
            CorsConfiguration cors = new CorsConfiguration();
            cors.setAllowedOrigins(origenes);
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            cors.setAllowedHeaders(List.of("*"));
            cors.setAllowCredentials(propiedades.login());
            fuente.registerCorsConfiguration("/api/**", cors);
        }
        return fuente;
    }
}
