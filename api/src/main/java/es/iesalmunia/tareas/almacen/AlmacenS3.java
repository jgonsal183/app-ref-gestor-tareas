package es.iesalmunia.tareas.almacen;

import es.iesalmunia.tareas.config.PropiedadesApp;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Adjuntos en un bucket de Amazon S3 (STORAGE=s3).
 *
 * No hay credenciales en la configuración: el SDK de AWS las busca solo
 * (variables AWS_ACCESS_KEY_ID..., fichero ~/.aws/credentials o, en una EC2,
 * el rol asociado a la instancia, que es lo recomendable).
 */
@Component
@ConditionalOnProperty(name = "app.almacenamiento.tipo", havingValue = "s3")
public class AlmacenS3 implements AlmacenAdjuntos {

    private static final Logger log = LoggerFactory.getLogger(AlmacenS3.class);

    private final S3Client s3;
    private final String bucket;

    public AlmacenS3(PropiedadesApp propiedades) {
        this.bucket = propiedades.almacenamiento().bucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Con STORAGE=s3 hay que indicar el bucket en S3_BUCKET");
        }
        S3ClientBuilder constructor = S3Client.builder();
        String region = propiedades.almacenamiento().region();
        if (region != null && !region.isBlank()) {
            constructor.region(Region.of(region));
        }
        this.s3 = constructor.build();
        log.info("Adjuntos en el bucket S3 {}", bucket);
    }

    @Override
    public void guardar(String clave, InputStream datos, long tamano, String tipoContenido) {
        PutObjectRequest peticion = PutObjectRequest.builder()
                .bucket(bucket)
                .key(clave)
                .contentType(tipoContenido)
                .contentLength(tamano)
                .build();
        s3.putObject(peticion, RequestBody.fromInputStream(datos, tamano));
    }

    @Override
    public InputStream abrir(String clave) {
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(clave).build());
    }

    @Override
    public void borrar(String clave) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
    }

    @Override
    public String descripcion() {
        return "s3 (" + bucket + ")";
    }

    @PreDestroy
    void cerrar() {
        s3.close();
    }
}
