# Gestor de tareas

Aplicación de referencia del módulo **Despliegue de Aplicaciones en Contenedores y Servicios en la Nube** (DACSN), 2º DAM, IES Almunia.

La aplicación se proporciona ya desarrollada: el trabajo del módulo es desplegarla de formas cada vez más evolucionadas (a mano en una VM, con contenedores, con imágenes propias y en AWS).

| Carpeta | Contenido |
|---|---|
| [`api/`](api/README.md) | API REST en Java con Spring Boot (PostgreSQL o MariaDB). |
| [`web/`](web/README.md) | Frontend estático en HTML, CSS y JavaScript, construido con Vite. |

Arquitectura:

```
navegador ──► nginx ──┬── /       ficheros estáticos del frontend
                      └── /api    proxy inverso ──► API (puerto 8080) ──► PostgreSQL o MariaDB
                                                                     ├──► Redis (opcional)
                                                                     ├──► SMTP / Mailpit (opcional)
                                                                     └──► disco local o S3 (adjuntos)
```

## Descargar la aplicación ya construida

Cada versión publicada en [Releases](https://github.com/jgonsal183/app-ref-gestor-tareas/releases) incluye la aplicación lista para desplegar, sin necesidad de compilar nada:

| Fichero | Contenido | Necesita |
|---|---|---|
| `gestor-tareas-X.Y.Z.jar` | La API | Java 21 (basta con el JRE) |
| `gestor-tareas-web-X.Y.Z.zip` | El frontend: ficheros estáticos para un servidor web | Nada |
| `SHA256SUMS` | Sumas de comprobación de los dos ficheros anteriores | — |

Para descargarlos desde la terminal:

```bash
VERSION=1.0.0
BASE=https://github.com/jgonsal183/app-ref-gestor-tareas/releases/download/v$VERSION

wget $BASE/gestor-tareas-$VERSION.jar
wget $BASE/gestor-tareas-web-$VERSION.zip
wget $BASE/SHA256SUMS
sha256sum -c SHA256SUMS   # comprueba que los ficheros han llegado íntegros
```

Se indica siempre una versión concreta, y no "la última", para que un despliegue sea reproducible: el mismo comando descarga siempre los mismos ficheros.

## Publicar una nueva versión

Las releases se generan automáticamente con GitHub Actions ([`.github/workflows/release.yml`](.github/workflows/release.yml)) al subir una etiqueta de versión:

```bash
git tag v1.1.0
git push origin v1.1.0
```

El número de la etiqueta pasa a ser la versión de la aplicación: aparece en el nombre de los ficheros y en `/api/info`.

## Qué no incluye este repositorio

Este repositorio **no incluye** Dockerfile, compose.yaml ni configuración de servidores: escribirlos forma parte del módulo.
