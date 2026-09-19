-- Datos de ejemplo (PostgreSQL)
-- Contraseña de ana y luis: Dacsn.2026 (se puede cambiar al arrancar con DEMO_PASSWORD).
-- El usuario invitado está inactivo: no puede iniciar sesión; se usa cuando LOGIN=false.

INSERT INTO usuario (nombre_usuario, nombre, contrasena, activo) VALUES
    ('invitado', 'Invitado', '{noop}sin-login', FALSE),
    ('ana', 'Ana Martín', '{bcrypt}$2a$10$43GYUBq7gvlwawWRBbFz0OEANDCQioyvE1KCSsB.TvaUnf0x5GOTu', TRUE),
    ('luis', 'Luis Ortega', '{bcrypt}$2a$10$43GYUBq7gvlwawWRBbFz0OEANDCQioyvE1KCSsB.TvaUnf0x5GOTu', TRUE);

-- Las fechas límite son relativas al día en que se crea la base de datos
INSERT INTO tarea (titulo, descripcion, estado, prioridad, fecha_limite, propietario_id) VALUES
    ('Importar la OVA de Debian en VirtualBox', 'Red en modo puente, MAC regenerada e IP estática.', 'HECHA', 'ALTA', CURRENT_DATE - 10, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Configurar el acceso por SSH con par de claves', 'Desactivar el acceso con contraseña y probar desde VS Code con Remote-SSH.', 'HECHA', 'ALTA', CURRENT_DATE - 8, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Hacer una instantánea de la VM', 'Antes de instalar nada más, para poder volver atrás.', 'EN_CURSO', 'MEDIA', CURRENT_DATE - 1, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Instalar Docker Engine desde el repositorio oficial', 'Con los plugins de Compose y buildx. Nada de Docker Desktop.', 'PENDIENTE', 'ALTA', CURRENT_DATE + 3, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Escribir el compose.yaml del gestor de tareas', 'Frontend, API, base de datos y Redis, con healthchecks y fichero .env.', 'PENDIENTE', 'MEDIA', CURRENT_DATE + 10, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Publicar la imagen de la API en Docker Hub', 'Con etiqueta de versión, no solo latest.', 'PENDIENTE', 'BAJA', CURRENT_DATE + 25, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Estimar el coste de la arquitectura en AWS', 'Con AWS Pricing Calculator; comparar después con el coste real.', 'PENDIENTE', 'BAJA', NULL, (SELECT id FROM usuario WHERE nombre_usuario = 'invitado')),
    ('Revisar el Dockerfile multi-stage', 'Comprobar el orden de las capas para aprovechar la caché de Maven.', 'EN_CURSO', 'ALTA', CURRENT_DATE + 2, (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('Escanear la imagen con Trivy', NULL, 'PENDIENTE', 'MEDIA', CURRENT_DATE + 5, (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('Documentar la imagen en el README', 'Variables, volúmenes y puertos.', 'PENDIENTE', 'MEDIA', CURRENT_DATE + 12, (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('Crear la VPC con dos zonas de disponibilidad', 'Subredes públicas y privadas; dibujar el diagrama.', 'PENDIENTE', 'BAJA', CURRENT_DATE + 40, (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('Configurar el Dev Container', 'devcontainer.json basado en Compose.', 'HECHA', 'MEDIA', CURRENT_DATE - 3, (SELECT id FROM usuario WHERE nombre_usuario = 'luis')),
    ('Probar el proxy inverso de nginx', 'Que /api llegue a la API y el resto al frontend.', 'PENDIENTE', 'ALTA', CURRENT_DATE - 2, (SELECT id FROM usuario WHERE nombre_usuario = 'luis')),
    ('Preparar la prueba de carga con k6', NULL, 'PENDIENTE', 'BAJA', NULL, (SELECT id FROM usuario WHERE nombre_usuario = 'luis'));
