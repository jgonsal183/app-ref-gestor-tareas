package es.iesalmunia.tareas.adjunto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Datos de un fichero adjunto a una tarea. El contenido del fichero no está en
 * la base de datos, sino en el almacén (disco local o S3), bajo la "clave".
 */
@Entity
@Table(name = "adjunto")
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarea_id", nullable = false)
    private Long tareaId;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido;

    @Column(nullable = false)
    private long tamano;

    @Column(nullable = false, unique = true, length = 64)
    private String clave;

    @Column(name = "subido_en", nullable = false)
    private Instant subidoEn;

    protected Adjunto() {
        // Requerido por JPA
    }

    public Adjunto(Long tareaId, String nombre, String tipoContenido, long tamano, String clave) {
        this.tareaId = tareaId;
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.tamano = tamano;
        this.clave = clave;
    }

    @PrePersist
    void alCrear() {
        subidoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTareaId() {
        return tareaId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public long getTamano() {
        return tamano;
    }

    public String getClave() {
        return clave;
    }

    public Instant getSubidoEn() {
        return subidoEn;
    }
}
