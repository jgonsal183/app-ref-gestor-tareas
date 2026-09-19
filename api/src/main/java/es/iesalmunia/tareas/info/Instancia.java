package es.iesalmunia.tareas.info;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Nombre de la máquina o contenedor donde se ejecuta la API.
 * En Docker es el identificador corto del contenedor; en una EC2, algo como
 * "ip-10-0-1-23". Sirve para ver qué instancia atiende cada petición.
 */
public final class Instancia {

    private static final String NOMBRE = calcularNombre();

    private Instancia() {
    }

    public static String nombre() {
        return NOMBRE;
    }

    private static String calcularNombre() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            String variable = System.getenv("HOSTNAME");
            return variable != null ? variable : "desconocida";
        }
    }
}
