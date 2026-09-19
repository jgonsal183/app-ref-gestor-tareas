package es.iesalmunia.tareas.almacen;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dónde se guardan los ficheros adjuntos. Hay dos implementaciones y se elige
 * con la variable STORAGE:
 *   - local: en un directorio del servidor (STORAGE_DIR); en Docker, un volumen.
 *   - s3:    en un bucket de Amazon S3 (S3_BUCKET).
 *
 * Cada fichero se identifica con una "clave" generada por la aplicación,
 * nunca con el nombre original, para evitar colisiones y nombres peligrosos.
 */
public interface AlmacenAdjuntos {

    void guardar(String clave, InputStream datos, long tamano, String tipoContenido) throws IOException;

    InputStream abrir(String clave) throws IOException;

    void borrar(String clave) throws IOException;

    /** Texto para /api/info, por ejemplo "local (/datos/adjuntos)" o "s3 (mi-bucket)". */
    String descripcion();
}
