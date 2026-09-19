import { defineConfig } from "vite";

// Dirección de la API durante el desarrollo (npm run dev).
// En producción no se usa: el frontend llama a /api y nginx hace de proxy.
const API_URL = process.env.API_URL || "http://localhost:8080";

export default defineConfig({
  // Rutas relativas en el HTML generado: el frontend funciona aunque se
  // publique en una subcarpeta o en un bucket S3.
  base: "./",

  // Servidor de desarrollo: accesible desde fuera de la VM (host: true) y
  // con un proxy de /api hacia la API, igual que hará nginx en producción.
  server: {
    host: true,
    port: 5173,
    proxy: { "/api": API_URL },
  },
  preview: {
    host: true,
    port: 4173,
    proxy: { "/api": API_URL },
  },
});
