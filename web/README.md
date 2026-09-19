# Gestor de tareas · Frontend

Interfaz web del gestor de tareas: HTML, CSS y JavaScript sin frameworks. Se construye con [Vite](https://vite.dev) y usa [Pico CSS](https://picocss.com) para los estilos base.

El resultado de la construcción es una carpeta `dist/` con **ficheros estáticos** que puede servir cualquier servidor web (nginx, Apache, un bucket S3...). No necesita Node.js para funcionar, solo para construirse.

## Requisitos

- Node.js 20.19 o posterior, con npm. Puedes comprobar la versión con `node -v`.
- Para usar la aplicación, la API en marcha (ver [`../api`](../api/README.md)).

## Construir

```bash
npm ci          # instala las dependencias exactas de package-lock.json
npm run build   # genera dist/
```

`npm ci` es la forma adecuada de instalar dependencias para construir: usa exactamente las versiones de `package-lock.json` y falla si `package.json` y el lock no coinciden. `npm install` puede actualizar el lock y está pensado para cuando se añaden o cambian dependencias.

Vite avisará de que `config.js` no se puede empaquetar. Es intencionado: ese fichero debe quedarse fuera (ver más abajo).

## Desarrollar

```bash
npm run dev
```

Arranca un servidor de desarrollo en el puerto 5173, accesible también desde fuera de la VM (`http://IP_DE_LA_VM:5173`). Recarga la página automáticamente al guardar cambios y reenvía las peticiones de `/api` a la API, que por defecto se busca en `http://localhost:8080`. Si la API está en otro sitio:

```bash
API_URL=http://otra-maquina:8080 npm run dev
```

`npm run preview` sirve la versión ya construida de `dist/` (puerto 4173), con el mismo reenvío de `/api`.

## Cómo encuentra la API

El frontend hace todas sus peticiones a rutas que empiezan por `/api`, **en el mismo servidor del que se ha descargado la página**. Por eso, en producción, el servidor web tiene que hacer dos cosas:

1. Servir los ficheros de `dist/`.
2. Reenviar las peticiones de `/api` a la API (proxy inverso), que escucha en el puerto 8080.

Si la API no es accesible por esa ruta, la aplicación lo indica al cargar: "No se encuentra la API", "El proxy no puede conectar con la API" o "La respuesta no viene de la API", según el caso.

Hay que tener en cuenta también el tamaño máximo de las peticiones: la API admite adjuntos de hasta 5 MB y el servidor web debe permitirlo. nginx, por ejemplo, rechaza por defecto las peticiones de más de 1 MB con un error 413.

### config.js: configuración en tiempo de ejecución

`public/config.js` se copia tal cual a `dist/config.js`, sin pasar por Vite, y el navegador lo carga antes que la aplicación:

```js
window.APP_CONFIG = {
  apiUrl: "",   // vacío = la API está en el mismo origen (/api)
};
```

Solo hay que cambiarlo si el frontend y la API están en orígenes distintos (por ejemplo, el frontend en un bucket S3 y la API detrás de un balanceador). En ese caso, `apiUrl` es la URL de la API y la API debe tener configurado `CORS_ORIGINS` con el origen del frontend.

Como no forma parte del código empaquetado, se puede modificar en el servidor **sin volver a construir** el frontend, por ejemplo generándolo al arrancar un contenedor a partir de una variable de entorno.

## Dependencias con binarios nativos

Vite usa internamente herramientas escritas en Rust (Rolldown y LightningCSS) que se distribuyen como binarios compilados para cada sistema. npm instala solo el que corresponde a la máquina: por ejemplo `lightningcss-linux-x64-gnu` en Debian o Ubuntu, y `lightningcss-linux-x64-musl` en Alpine. Por eso:

- `node_modules/` no se copia de una máquina a otra: se instala en cada sitio con `npm ci`.
- `node_modules/` y `dist/` no se suben al repositorio (están en `.gitignore`).

## Estructura

```
web/
├── index.html          página única de la aplicación
├── package.json        dependencias y scripts
├── package-lock.json   versiones exactas de todas las dependencias
├── vite.config.js      configuración de Vite (rutas relativas, proxy de desarrollo)
├── public/             ficheros que se copian tal cual a dist/
│   ├── config.js       configuración en tiempo de ejecución
│   └── favicon.svg
└── src/
    ├── main.js         lógica de la interfaz
    ├── api.js          llamadas a la API
    └── estilos.css     estilos propios
```
