package es.iesalmunia.tareas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Envío de correo por SMTP. Solo existe si la variable MAIL_HOST tiene valor.
 * En desarrollo se usa Mailpit, que acepta cualquier correo y lo muestra en su web.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("'${app.correo.host:}' != ''")
public class CorreoConfig {

    @Bean
    JavaMailSenderImpl javaMailSender(PropiedadesApp propiedades) {
        JavaMailSenderImpl remitente = new JavaMailSenderImpl();
        remitente.setHost(propiedades.correo().host());
        remitente.setPort(propiedades.correo().port());
        remitente.setDefaultEncoding("UTF-8");
        return remitente;
    }
}
