package es.iesalmunia.tareas.correo;

public class CorreoDesactivadoException extends RuntimeException {

    public CorreoDesactivadoException() {
        super("El envío de correo no está configurado: define la variable MAIL_HOST");
    }
}
