package es.iesalmunia.tareas.correo;

import es.iesalmunia.tareas.config.PropiedadesApp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correos. Si MAIL_HOST no está definido, el correo está desactivado
 * y cualquier intento de envío produce un error explicativo.
 *
 * Punto de partida para ampliar la aplicación (por ejemplo, enviar un resumen
 * de las tareas pendientes o avisar de las que están vencidas).
 */
@Service
public class CorreoService {

    private final ObjectProvider<JavaMailSender> remitente;
    private final PropiedadesApp propiedades;

    public CorreoService(ObjectProvider<JavaMailSender> remitente, PropiedadesApp propiedades) {
        this.remitente = remitente;
        this.propiedades = propiedades;
    }

    public boolean activo() {
        return remitente.getIfAvailable() != null;
    }

    public void enviar(String destinatario, String asunto, String texto) {
        JavaMailSender servidor = remitente.getIfAvailable();
        if (servidor == null) {
            throw new CorreoDesactivadoException();
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(propiedades.correo().remitente());
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        servidor.send(mensaje);
    }
}
