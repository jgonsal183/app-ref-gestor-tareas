// Acceso a la API REST. Todas las llamadas pasan por la función peticion().

// URL base de la API, leída de config.js. Vacía = mismo origen.
const BASE = (window.APP_CONFIG?.apiUrl ?? "").replace(/\/$/, "");

/** Error devuelto por la API, con el código HTTP y, si lo hay, el detalle por campo. */
export class ErrorApi extends Error {
  constructor(estado, mensaje, errores = null) {
    super(mensaje);
    this.estado = estado;
    this.errores = errores;
  }
}

// Mensajes para errores que no vienen de la API sino de lo que hay delante
// (nginx, un balanceador...), que suelen responder con una página HTML.
const MENSAJES_HTTP = {
  404: "No se encuentra la API (¿está configurado el proxy de /api?)",
  413: "El fichero es demasiado grande para el servidor",
  502: "El proxy no puede conectar con la API (¿está arrancada?)",
  503: "La API no está disponible en este momento",
  504: "La API tarda demasiado en responder",
};

async function peticion(metodo, ruta, cuerpo) {
  const opciones = { method: metodo, headers: {} };
  if (cuerpo instanceof FormData || cuerpo instanceof URLSearchParams) {
    opciones.body = cuerpo; // el navegador pone el Content-Type adecuado
  } else if (cuerpo !== undefined) {
    opciones.headers["Content-Type"] = "application/json";
    opciones.body = JSON.stringify(cuerpo);
  }

  let respuesta;
  try {
    respuesta = await fetch(BASE + ruta, opciones);
  } catch {
    throw new ErrorApi(0, "No se puede conectar con la API");
  }

  const tipo = respuesta.headers.get("Content-Type") || "";
  const esJson = tipo.includes("json");

  if (!respuesta.ok) {
    let mensaje = MENSAJES_HTTP[respuesta.status] || `Error ${respuesta.status}`;
    let errores = null;
    if (esJson) {
      // Errores de la API en formato Problem Details: {status, detail, errores}
      const problema = await respuesta.json().catch(() => ({}));
      mensaje = problema.detail || problema.title || mensaje;
      errores = problema.errores || null;
    }
    throw new ErrorApi(respuesta.status, mensaje, errores);
  }

  if (esJson) {
    return respuesta.json();
  }
  if (respuesta.status === 200 && ruta.startsWith("/api/")) {
    // Una respuesta 200 que no es JSON suele indicar que la petición no ha
    // llegado a la API, sino al servidor de ficheros estáticos.
    throw new ErrorApi(respuesta.status, "La respuesta no viene de la API (¿está configurado el proxy de /api?)");
  }
  return null;
}

/** Construye "?estado=...&texto=..." omitiendo los filtros vacíos. */
function consulta(filtros) {
  const parametros = new URLSearchParams();
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor) parametros.set(clave, valor);
  }
  const texto = parametros.toString();
  return texto ? `?${texto}` : "";
}

export const api = {
  info: () => peticion("GET", "/api/info"),

  yo: () => peticion("GET", "/api/auth/yo"),
  login: (usuario, contrasena) =>
    peticion("POST", "/api/auth/login", new URLSearchParams({ usuario, contrasena })),
  logout: () => peticion("POST", "/api/auth/logout"),

  listarTareas: (filtros = {}) => peticion("GET", "/api/tareas" + consulta(filtros)),
  estadisticas: () => peticion("GET", "/api/tareas/estadisticas"),
  crearTarea: (datos) => peticion("POST", "/api/tareas", datos),
  actualizarTarea: (id, datos) => peticion("PUT", `/api/tareas/${id}`, datos),
  cambiarEstado: (id, estado) => peticion("PATCH", `/api/tareas/${id}/estado`, { estado }),
  borrarTarea: (id) => peticion("DELETE", `/api/tareas/${id}`),

  listarAdjuntos: (tareaId) => peticion("GET", `/api/tareas/${tareaId}/adjuntos`),
  subirAdjunto: (tareaId, fichero) => {
    const datos = new FormData();
    datos.append("fichero", fichero);
    return peticion("POST", `/api/tareas/${tareaId}/adjuntos`, datos);
  },
  borrarAdjunto: (id) => peticion("DELETE", `/api/adjuntos/${id}`),
  urlAdjunto: (id) => `${BASE}/api/adjuntos/${id}`,
};
