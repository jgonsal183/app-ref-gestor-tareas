-- Esquema inicial del gestor de tareas (MariaDB)

CREATE TABLE usuario (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50)  NOT NULL UNIQUE,
    nombre         VARCHAR(100) NOT NULL,
    contrasena     VARCHAR(100) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tarea (
    id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo         VARCHAR(120)  NOT NULL,
    descripcion    VARCHAR(2000),
    estado         VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    prioridad      VARCHAR(20)   NOT NULL DEFAULT 'MEDIA',
    fecha_limite   DATE,
    propietario_id BIGINT        NOT NULL,
    creada_en      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    actualizada_en DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_tarea_propietario FOREIGN KEY (propietario_id) REFERENCES usuario (id),
    CONSTRAINT ck_tarea_estado      CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'HECHA')),
    CONSTRAINT ck_tarea_prioridad   CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE adjunto (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tarea_id       BIGINT       NOT NULL,
    nombre         VARCHAR(255) NOT NULL,
    tipo_contenido VARCHAR(100),
    tamano         BIGINT       NOT NULL,
    clave          VARCHAR(64)  NOT NULL UNIQUE,
    subido_en      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_adjunto_tarea FOREIGN KEY (tarea_id) REFERENCES tarea (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
