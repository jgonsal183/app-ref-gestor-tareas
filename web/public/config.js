// Configuración del frontend EN TIEMPO DE EJECUCIÓN.
//
// Este fichero no pasa por Vite: se copia tal cual a dist/ y el navegador lo
// carga antes que la aplicación. Por eso se puede cambiar en el servidor sin
// volver a construir el frontend (por ejemplo, generándolo al arrancar un
// contenedor a partir de variables de entorno).
window.APP_CONFIG = {
  // URL base de la API, sin "/" final.
  // Vacía: la API está en el mismo origen que el frontend (nginx hace de
  // proxy de /api). Solo hay que cambiarla si el frontend se sirve desde otro
  // sitio, por ejemplo https://api.midominio.com
  apiUrl: "",
};
