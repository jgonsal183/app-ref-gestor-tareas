# Gestor de tareas · API

API REST de la aplicación de referencia del módulo **Despliegue de Aplicaciones en Contenedores y Servicios en la Nube** (DACSN), IES Almunia.

Está hecha con Spring Boot 4.1 y Java 21. Guarda los datos en **PostgreSQL o MariaDB**, según la URL de conexión. Redis, el correo y S3 son opcionales: se activan con variables de entorno.

## Requisitos

- JDK 21 o posterior para compilar. Para ejecutar el `.jar` basta con un JRE 21 o posterior.
- Una base de datos PostgreSQL o MariaDB con una base de datos vacía y un usuario con permisos sobre ella. Las tablas y los datos de ejemplo los crea la propia aplicación al arrancar (Flyway).
- Opcionales: Redis, un servidor SMTP (Mailpit en desarrollo) y un bucket de S3.

No hace falta instalar Maven: el proyecto incluye el *Maven wrapper* (`mvnw`), que descarga la versión adecuada la primera vez.

## Compilar

```bash
./mvnw package                    # compila, pasa las pruebas y genera el .jar
./mvnw package -DskipTests        # sin pasar las pruebas
./mvnw package -Drevision=1.2.0   # con otro número de versión
```

El resultado es `target/gestor-tareas-1.0.0.jar`, un único fichero que contiene la aplicación y todas sus dependencias. Las pruebas no necesitan base de datos.

## Ejecutar

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tareas
export DB_USER=tareas
export DB_PASSWORD=tareas
java -jar target/gestor-tareas-1.0.0.jar
```

Para usar MariaDB solo cambia la URL: `jdbc:mariadb://localhost:3306/tareas`.

La API escucha en el puerto 8080. Para comprobar que funciona:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/info
curl http://localhost:8080/api/tareas
```

## Variables de entorno

| Variable | Valor por defecto | Para qué sirve |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/tareas` | URL JDBC. El SGBD se deduce de ella (`postgresql` o `mariadb`). |
| `DB_USER` | `tareas` | Usuario de la base de datos. |
| `DB_PASSWORD` | `tareas` | Contraseña de la base de datos. |
| `SERVER_PORT` | `8080` | Puerto en el que escucha la API. |
| `LOGIN` | `false` | `true` para exigir inicio de sesión. |
| `DEMO_PASSWORD` | *(vacía)* | Si se indica, sustituye al arrancar la contraseña de los usuarios de ejemplo. |
| `REDIS_HOST` | *(vacía)* | Si se indica, se usa Redis para la caché y las sesiones. |
| `REDIS_PORT` | `6379` | Puerto de Redis. |
| `REDIS_PASSWORD` | *(vacía)* | Contraseña de Redis, si la tiene. |
| `STORAGE` | `local` | Dónde se guardan los adjuntos: `local` o `s3`. |
| `STORAGE_DIR` | `./datos/adjuntos` | Directorio de los adjuntos con `STORAGE=local`. |
| `S3_BUCKET` | *(vacía)* | Bucket de los adjuntos con `STORAGE=s3`. |
| `S3_REGION` | *(vacía)* | Región del bucket. Si está vacía, se toma la del entorno (`AWS_REGION` o la de la instancia EC2). |
| `MAIL_HOST` | *(vacía)* | Si se indica, se activa el envío de correo por SMTP. |
| `MAIL_PORT` | `1025` | Puerto SMTP (1025 es el de Mailpit). |
| `MAIL_FROM` | `tareas@dacsn.local` | Remitente de los correos. |
| `CORS_ORIGINS` | *(vacía)* | Orígenes permitidos, separados por comas. Solo hace falta si el frontend se sirve desde otro dominio. |

Con STORAGE=s3 no se configuran credenciales: el SDK de AWS las busca por sí mismo. En una EC2, lo recomendable es asociar a la instancia un rol con permisos sobre el bucket.

## Modos de funcionamiento

| Configuración | Comportamiento |
|---|---|
| `LOGIN=false` (por defecto) | No hay que iniciar sesión. Todo el mundo trabaja como el usuario `invitado` y no se crean sesiones. |
| `LOGIN=true`, sin `REDIS_HOST` | Hay que iniciar sesión. La sesión se guarda en la memoria de la instancia: si hay varias instancias detrás de un balanceador, cada una conoce solo sus propias sesiones. |
| `LOGIN=true` y `REDIS_HOST` | Hay que iniciar sesión. Las sesiones se guardan en Redis y todas las instancias las comparten. |

Con Redis activo, la caché de estadísticas también se comparte. Las estadísticas indican qué instancia las calculó (`generadoPor`), así que puede verse cómo una instancia sirve datos calculados por otra.

## Usuarios de ejemplo

| Usuario | Contraseña | Observaciones |
|---|---|---|
| `ana` | `Dacsn.2026` | 4 tareas |
| `luis` | `Dacsn.2026` | 3 tareas |
| `invitado` | — | No puede iniciar sesión. Es el usuario que se usa con `LOGIN=false` (7 tareas). |

En un servidor accesible desde Internet, arranca con `DEMO_PASSWORD` para no dejar una contraseña conocida.

## Endpoints

| Método y ruta | Descripción |
|---|---|
| `GET /api/info` | Datos del despliegue: versión, instancia, SGBD, caché, sesiones, almacenamiento y correo. |
| `GET /api/tareas` | Tareas del usuario. Filtros opcionales: `?estado=PENDIENTE&prioridad=ALTA&texto=docker`. |
| `GET /api/tareas/{id}` | Una tarea. |
| `POST /api/tareas` | Crea una tarea. |
| `PUT /api/tareas/{id}` | Modifica una tarea. |
| `PATCH /api/tareas/{id}/estado` | Cambia solo el estado: `{"estado": "HECHA"}`. |
| `DELETE /api/tareas/{id}` | Borra una tarea y sus adjuntos. |
| `GET /api/tareas/estadisticas` | Recuento por estado y tareas vencidas (con caché). |
| `GET /api/tareas/{id}/adjuntos` | Adjuntos de una tarea. |
| `POST /api/tareas/{id}/adjuntos` | Sube un adjunto (multipart, campo `fichero`, máximo 5 MB). |
| `GET /api/adjuntos/{id}` | Descarga un adjunto. |
| `DELETE /api/adjuntos/{id}` | Borra un adjunto. |
| `GET /api/auth/yo` | Usuario actual. Con `LOGIN=true` y sin sesión iniciada, responde 401. |
| `POST /api/auth/login` | Inicia sesión (formulario con los campos `usuario` y `contrasena`). |
| `POST /api/auth/logout` | Cierra la sesión. |
| `POST /api/correo/prueba` | Envía un correo de prueba: `{"destinatario": "alguien@ejemplo.com"}`. |
| `GET /actuator/health` | Estado de la API y de sus dependencias (base de datos y, si está activo, Redis). |

Estados de una tarea: `PENDIENTE`, `EN_CURSO` y `HECHA`. Prioridades: `BAJA`, `MEDIA` y `ALTA`.

Los errores se devuelven en formato *Problem Details* (RFC 9457), por ejemplo `{"status": 404, "detail": "No existe la tarea 99"}`.

### Ejemplos con curl

```bash
# Crear una tarea
curl -X POST http://localhost:8080/api/tareas \
     -H 'Content-Type: application/json' \
     -d '{"titulo": "Probar la API", "prioridad": "ALTA", "fechaLimite": "2026-12-01"}'

# Marcarla como hecha
curl -X PATCH http://localhost:8080/api/tareas/1/estado \
     -H 'Content-Type: application/json' -d '{"estado": "HECHA"}'

# Subir un adjunto
curl -F fichero=@notas.txt http://localhost:8080/api/tareas/1/adjuntos

# Con LOGIN=true: iniciar sesión guardando la cookie y usarla después
curl -c cookies.txt -d 'usuario=ana&contrasena=Dacsn.2026' http://localhost:8080/api/auth/login
curl -b cookies.txt http://localhost:8080/api/tareas
```

## Estructura del proyecto

```
api/
├── mvnw, mvnw.cmd, .mvn/       Maven wrapper
├── pom.xml                     dependencias y construcción
└── src/
    ├── main/java/es/iesalmunia/tareas/
    │   ├── config/             configuración: seguridad, Redis, correo, propiedades
    │   ├── tarea/              tareas: entidad, repositorio, servicio y controlador
    │   ├── adjunto/            adjuntos de las tareas
    │   ├── almacen/            dónde se guardan los ficheros: local o S3
    │   ├── usuario/            usuarios y login
    │   ├── info/               /api/info
    │   ├── correo/             envío de correo
    │   └── error/              respuestas de error
    ├── main/resources/
    │   ├── application.yaml    configuración (lee las variables de entorno)
    │   └── db/migration/       esquema y datos de ejemplo, uno por SGBD
    └── test/                   pruebas unitarias
```
