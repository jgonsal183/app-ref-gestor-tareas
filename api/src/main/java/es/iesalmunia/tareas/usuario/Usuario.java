package es.iesalmunia.tareas.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario de la aplicación. Los usuarios se crean con las migraciones de Flyway
 * (no hay registro desde la web).
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    /** Usuario que se usa cuando el login está desactivado (LOGIN=false). */
    public static final String INVITADO = "invitado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Contraseña cifrada, con el prefijo del algoritmo: {bcrypt}$2a$10$... */
    @Column(nullable = false, length = 100)
    private String contrasena;

    /** Un usuario inactivo no puede iniciar sesión (es el caso de "invitado"). */
    @Column(nullable = false)
    private boolean activo;

    protected Usuario() {
        // Requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isActivo() {
        return activo;
    }
}
