-- Datos de ejemplo (MariaDB)
-- Contraseña de ana y luis: Dacsn.2026 (se puede cambiar al arrancar con DEMO_PASSWORD).
-- El usuario invitado está inactivo: no puede iniciar sesión; se usa cuando LOGIN=false.

INSERT INTO usuario (nombre_usuario, nombre, contrasena, activo) VALUES
    ('invitado', 'Invitado', '{noop}sin-login', FALSE),
    ('ana', 'Ana Martín', '{bcrypt}$2a$10$43GYUBq7gvlwawWRBbFz0OEANDCQioyvE1KCSsB.TvaUnf0x5GOTu', TRUE),
    ('luis', 'Luis Ortega', '{bcrypt}$2a$10$43GYUBq7gvlwawWRBbFz0OEANDCQioyvE1KCSsB.TvaUnf0x5GOTu', TRUE);

-- Las fechas límite son relativas al día en que se crea la base de datos
INSERT INTO tarea (titulo, descripcion, estado, prioridad, fecha_limite, propietario_id, creada_en, actualizada_en) VALUES
    ('Importar la OVA de Debian en VirtualBox', 'Red en modo puente, MAC regenerada e IP estática.', 'HECHA', 'ALTA', CURRENT_DATE - INTERVAL 10 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Configurar el acceso por SSH con par de claves', 'Desactivar el acceso con contraseña y probar desde VS Code con Remote-SSH.', 'HECHA', 'ALTA', CURRENT_DATE - INTERVAL 8 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Hacer una instantánea de la VM', 'Antes de instalar nada más, para poder volver atrás.', 'EN_CURSO', 'MEDIA', CURRENT_DATE - INTERVAL 1 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Instalar Docker Engine desde el repositorio oficial', 'Con los plugins de Compose y buildx. Nada de Docker Desktop.', 'PENDIENTE', 'ALTA', CURRENT_DATE + INTERVAL 3 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Escribir el compose.yaml del gestor de tareas', 'Frontend, API, base de datos y Redis, con healthchecks y fichero .env.', 'PENDIENTE', 'MEDIA', CURRENT_DATE + INTERVAL 10 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Publicar la imagen de la API en Docker Hub', 'Con etiqueta de versión, no solo latest.', 'PENDIENTE', 'BAJA', CURRENT_DATE + INTERVAL 25 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Estimar el coste de la arquitectura en AWS', 'Con AWS Pricing Calculator; comparar después con el coste real.', 'PENDIENTE', 'BAJA', NULL, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Revisar el Dockerfile multi-stage', 'Comprobar el orden de las capas para aprovechar la caché de Maven.', 'EN_CURSO', 'ALTA', CURRENT_DATE + INTERVAL 2 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'ana'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Escanear la imagen con Trivy', NULL, 'PENDIENTE', 'MEDIA', CURRENT_DATE + INTERVAL 5 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'ana'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Documentar la imagen en el README', 'Variables, volúmenes y puertos.', 'PENDIENTE', 'MEDIA', CURRENT_DATE + INTERVAL 12 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'ana'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Crear la VPC con dos zonas de disponibilidad', 'Subredes públicas y privadas; dibujar el diagrama.', 'PENDIENTE', 'BAJA', CURRENT_DATE + INTERVAL 40 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'ana'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Configurar el Dev Container', 'devcontainer.json basado en Compose.', 'HECHA', 'MEDIA', CURRENT_DATE - INTERVAL 3 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'luis'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Probar el proxy inverso de nginx', 'Que /api llegue a la API y el resto al frontend.', 'PENDIENTE', 'ALTA', CURRENT_DATE - INTERVAL 2 DAY, (SELECT id FROM usuario WHERE nombre_usuario = 'luis'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('Preparar la prueba de carga con k6', NULL, 'PENDIENTE', 'BAJA', NULL, (SELECT id FROM usuario WHERE nombre_usuario = 'luis'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
