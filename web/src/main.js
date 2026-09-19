// Interfaz del gestor de tareas: JavaScript sin frameworks.
// Los estilos base son de Pico CSS; estilos.css añade los detalles propios.

import "@picocss/pico/css/pico.min.css";
import "./estilos.css";
import { api, ErrorApi } from "./api.js";

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

const $ = (selector) => document.querySelector(selector);

/**
 * Crea un elemento HTML: el("p", {class: "x"}, "texto", otroElemento).
 * El texto se inserta como texto, nunca como HTML, así que lo que escriba un
 * usuario no puede inyectar código en la página.
 */
function el(etiqueta, atributos = {}, ...hijos) {
  const elemento = document.createElement(etiqueta);
  for (const [nombre, valor] of Object.entries(atributos)) {
    if (nombre.startsWith("on")) {
      elemento.addEventListener(nombre.slice(2), valor);
    } else if (valor === true) {
      elemento.setAttribute(nombre, "");
    } else if (valor !== false && valor != null) {
      elemento.setAttribute(nombre, valor);
    }
  }
  elemento.append(...hijos.filter((hijo) => hijo != null));
  return elemento;
}

const NOMBRES_ESTADO = { PENDIENTE: "Pendiente", EN_CURSO: "En curso", HECHA: "Hecha" };
const NOMBRES_PRIORIDAD = { BAJA: "Baja", MEDIA: "Media", ALTA: "Alta" };

const formatoRelativo = new Intl.RelativeTimeFormat("es", { numeric: "auto" });
const formatoFecha = new Intl.DateTimeFormat("es-ES", { dateStyle: "medium" });
const formatoHora = new Intl.DateTimeFormat("es-ES", { timeStyle: "medium" });

/** "2026-10-03" → texto como "vence mañana" o "venció hace 2 días". */
function textoFechaLimite(fechaIso) {
  const [anio, mes, dia] = fechaIso.split("-").map(Number);
  const fecha = new Date(anio, mes - 1, dia);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const dias = Math.round((fecha - hoy) / 86_400_000);
  const relativo = formatoRelativo.format(dias, "day");
  return `${dias < 0 ? "Venció" : "Vence"} ${relativo} (${formatoFecha.format(fecha)})`;
}

function tamanoLegible(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

let temporizadorAviso;
function avisar(mensaje, tipo = "info") {
  const aviso = $("#aviso");
  aviso.textContent = mensaje;
  aviso.className = tipo;
  aviso.hidden = false;
  clearTimeout(temporizadorAviso);
  temporizadorAviso = setTimeout(() => (aviso.hidden = true), 4000);
}

/** Muestra un error de la API. Si la sesión ha caducado, vuelve al login. */
function tratarError(error) {
  if (error instanceof ErrorApi && error.estado === 401) {
    mostrarVista("login");
    avisar("La sesión ha caducado o no es válida. Vuelve a iniciar sesión.", "error");
    return;
  }
  console.error(error);
  avisar(error.message, "error");
}

// ---------------------------------------------------------------------------
// Vistas
// ---------------------------------------------------------------------------

function mostrarVista(nombre) {
  $("#cargando").hidden = true;
  for (const vista of ["error", "login", "tareas"]) {
    $(`#vista-${vista}`).hidden = vista !== nombre;
  }
  if (nombre !== "tareas") $("#zona-usuario").replaceChildren();
}

async function iniciar() {
  $("#cargando").hidden = false;
  cargarPie();
  try {
    const yo = await api.yo();
    entrarEnLaAplicacion(yo);
  } catch (error) {
    if (error instanceof ErrorApi && error.estado === 401) {
      mostrarVista("login");
    } else {
      $("#texto-error").textContent = error.message;
      mostrarVista("error");
    }
  }
}

function entrarEnLaAplicacion(yo) {
  const zona = $("#zona-usuario");
  zona.replaceChildren(el("li", {}, el("small", {}, yo.nombre)));
  if (yo.loginActivo) {
    zona.append(el("li", {}, el("button", { class: "secondary outline", onclick: cerrarSesion }, "Salir")));
  }
  mostrarVista("tareas");
  recargar();
}

// --- Inicio y cierre de sesión ---

$("#formulario-login").addEventListener("submit", async (evento) => {
  evento.preventDefault();
  const datos = new FormData(evento.target);
  const errorLogin = $("#error-login");
  errorLogin.textContent = "";
  try {
    await api.login(datos.get("usuario"), datos.get("contrasena"));
    evento.target.reset();
    entrarEnLaAplicacion(await api.yo());
  } catch (error) {
    errorLogin.textContent =
      error.estado === 401 ? "Usuario o contraseña incorrectos" : error.message;
  }
});

async function cerrarSesion() {
  try {
    await api.logout();
  } finally {
    mostrarVista("login");
  }
}

// ---------------------------------------------------------------------------
// Tareas
// ---------------------------------------------------------------------------

function filtrosActuales() {
  const datos = new FormData($("#filtros"));
  return {
    estado: datos.get("estado"),
    prioridad: datos.get("prioridad"),
    texto: datos.get("texto").trim(),
  };
}

async function recargar() {
  await Promise.all([cargarTareas(), cargarEstadisticas()]);
}

async function cargarTareas() {
  const lista = $("#lista-tareas");
  lista.setAttribute("aria-busy", "true");
  try {
    const tareas = await api.listarTareas(filtrosActuales());
    lista.replaceChildren(
      ...(tareas.length ? tareas.map(tarjetaTarea) : [el("p", { class: "vacio" }, "No hay tareas que mostrar.")])
    );
  } catch (error) {
    tratarError(error);
  } finally {
    lista.removeAttribute("aria-busy");
  }
}

function tarjetaTarea(tarea) {
  const clases = ["tarea", `prioridad-${tarea.prioridad.toLowerCase()}`];
  if (tarea.vencida) clases.push("vencida");
  if (tarea.estado === "HECHA") clases.push("hecha");

  const selectorEstado = el(
    "select",
    {
      "aria-label": "Estado",
      onchange: async (evento) => {
        try {
          await api.cambiarEstado(tarea.id, evento.target.value);
          recargar();
        } catch (error) {
          tratarError(error);
        }
      },
    },
    ...Object.entries(NOMBRES_ESTADO).map(([valor, texto]) =>
      el("option", { value: valor, selected: valor === tarea.estado }, texto)
    )
  );

  return el(
    "article",
    { class: clases.join(" ") },
    el(
      "div",
      { class: "cabecera-tarea" },
      el("span", { class: "etiqueta-prioridad" }, NOMBRES_PRIORIDAD[tarea.prioridad]),
      el("strong", {}, tarea.titulo)
    ),
    tarea.descripcion ? el("p", {}, tarea.descripcion) : null,
    el(
      "div",
      { class: "pie-tarea" },
      el("small", { class: "fecha" }, tarea.fechaLimite ? textoFechaLimite(tarea.fechaLimite) : "Sin fecha límite"),
      el(
        "div",
        { class: "acciones" },
        selectorEstado,
        el("button", { class: "secondary outline", onclick: () => abrirDialogoTarea(tarea) }, "Editar"),
        el("button", { class: "contrast outline", onclick: () => borrarTarea(tarea) }, "Borrar")
      )
    )
  );
}

async function borrarTarea(tarea) {
  if (!confirm(`¿Borrar la tarea «${tarea.titulo}» y sus adjuntos?`)) return;
  try {
    await api.borrarTarea(tarea.id);
    avisar("Tarea borrada");
    recargar();
  } catch (error) {
    tratarError(error);
  }
}

async function cargarEstadisticas() {
  try {
    const estadisticas = await api.estadisticas();
    const caja = (numero, texto, clase = "") =>
      el("div", { class: `caja ${clase}` }, el("strong", {}, String(numero)), el("small", {}, texto));
    $("#estadisticas").replaceChildren(
      caja(estadisticas.porEstado.PENDIENTE ?? 0, "Pendientes"),
      caja(estadisticas.porEstado.EN_CURSO ?? 0, "En curso"),
      caja(estadisticas.porEstado.HECHA ?? 0, "Hechas"),
      caja(estadisticas.vencidas, "Vencidas", estadisticas.vencidas ? "alerta" : "")
    );
    // De dónde sale el dato: útil para ver la caché compartida entre instancias
    $("#origen-estadisticas").textContent =
      `Calculadas por ${estadisticas.generadoPor} a las ${formatoHora.format(new Date(estadisticas.generadoEn))}`;
  } catch (error) {
    tratarError(error);
  }
}

// --- Filtros ---

let temporizadorBusqueda;
$("#filtros").addEventListener("input", (evento) => {
  // Al escribir en el buscador se espera un poco para no hacer una petición por tecla
  clearTimeout(temporizadorBusqueda);
  const espera = evento.target.name === "texto" ? 300 : 0;
  temporizadorBusqueda = setTimeout(cargarTareas, espera);
});
$("#filtros").addEventListener("submit", (evento) => evento.preventDefault());

// ---------------------------------------------------------------------------
// Diálogo de crear y editar tareas
// ---------------------------------------------------------------------------

let tareaEnEdicion = null; // null = creando una tarea nueva

function abrirDialogoTarea(tarea = null) {
  tareaEnEdicion = tarea;
  const formulario = $("#formulario-tarea");
  formulario.reset();
  $("#error-tarea").textContent = "";
  $("#titulo-dialogo").textContent = tarea ? "Editar tarea" : "Nueva tarea";

  if (tarea) {
    formulario.titulo.value = tarea.titulo;
    formulario.descripcion.value = tarea.descripcion ?? "";
    formulario.prioridad.value = tarea.prioridad;
    formulario.estado.value = tarea.estado;
    formulario.fechaLimite.value = tarea.fechaLimite ?? "";
    cargarAdjuntos();
  }
  $("#seccion-adjuntos").hidden = !tarea;
  $("#dialogo-tarea").showModal();
}

$("#nueva-tarea").addEventListener("click", () => abrirDialogoTarea());

$("#formulario-tarea").addEventListener("submit", async (evento) => {
  evento.preventDefault();
  const formulario = evento.target;
  const datos = {
    titulo: formulario.titulo.value.trim(),
    descripcion: formulario.descripcion.value.trim() || null,
    prioridad: formulario.prioridad.value,
    estado: formulario.estado.value,
    fechaLimite: formulario.fechaLimite.value || null,
  };
  try {
    if (tareaEnEdicion) {
      await api.actualizarTarea(tareaEnEdicion.id, datos);
      avisar("Tarea guardada");
    } else {
      await api.crearTarea(datos);
      avisar("Tarea creada");
    }
    $("#dialogo-tarea").close();
    recargar();
  } catch (error) {
    if (error.errores) {
      // Errores de validación: se muestran junto al formulario
      $("#error-tarea").textContent = Object.values(error.errores).join(". ");
    } else {
      tratarError(error);
    }
  }
});

// --- Adjuntos ---

async function cargarAdjuntos() {
  const lista = $("#lista-adjuntos");
  lista.replaceChildren();
  try {
    const adjuntos = await api.listarAdjuntos(tareaEnEdicion.id);
    if (!adjuntos.length) {
      lista.append(el("li", { class: "vacio" }, "Esta tarea no tiene adjuntos."));
    }
    for (const adjunto of adjuntos) {
      lista.append(
        el(
          "li",
          {},
          el("a", { href: api.urlAdjunto(adjunto.id), download: adjunto.nombre }, adjunto.nombre),
          el("small", {}, ` ${tamanoLegible(adjunto.tamano)} `),
          el("button", { class: "enlace", onclick: () => borrarAdjunto(adjunto) }, "Quitar")
        )
      );
    }
  } catch (error) {
    tratarError(error);
  }
}

$("#formulario-adjunto").addEventListener("submit", async (evento) => {
  evento.preventDefault();
  const fichero = evento.target.fichero.files[0];
  if (!fichero) return;
  const boton = evento.submitter;
  boton.setAttribute("aria-busy", "true");
  try {
    await api.subirAdjunto(tareaEnEdicion.id, fichero);
    evento.target.reset();
    avisar("Fichero subido");
    cargarAdjuntos();
  } catch (error) {
    tratarError(error);
  } finally {
    boton.removeAttribute("aria-busy");
  }
});

async function borrarAdjunto(adjunto) {
  if (!confirm(`¿Quitar el adjunto «${adjunto.nombre}»?`)) return;
  try {
    await api.borrarAdjunto(adjunto.id);
    cargarAdjuntos();
  } catch (error) {
    tratarError(error);
  }
}

// ---------------------------------------------------------------------------
// Datos del despliegue (/api/info)
// ---------------------------------------------------------------------------

const ETIQUETAS_INFO = {
  version: "Versión",
  instancia: "Instancia",
  arrancadaEn: "Arrancada",
  java: "Java",
  baseDatos: "Base de datos",
  login: "Login",
  sesiones: "Sesiones",
  cache: "Caché",
  almacenamiento: "Adjuntos",
  correo: "Correo",
};

async function cargarPie() {
  try {
    const info = await api.info();
    $("#pie-info").textContent = `Versión ${info.version} · instancia ${info.instancia}`;
  } catch {
    $("#pie-info").textContent = "API no disponible";
  }
}

$("#ver-info").addEventListener("click", async (evento) => {
  evento.preventDefault();
  try {
    const info = await api.info();
    $("#tabla-info").replaceChildren(
      ...Object.entries(ETIQUETAS_INFO).map(([clave, etiqueta]) => {
        let valor = info[clave];
        if (typeof valor === "boolean") valor = valor ? "activado" : "desactivado";
        if (clave === "arrancadaEn") valor = new Date(valor).toLocaleString("es-ES");
        return el("tr", {}, el("th", { scope: "row" }, etiqueta), el("td", {}, String(valor)));
      })
    );
    $("#pie-info").textContent = `Versión ${info.version} · instancia ${info.instancia}`;
    $("#dialogo-info").showModal();
  } catch (error) {
    tratarError(error);
  }
});

// ---------------------------------------------------------------------------
// Arranque
// ---------------------------------------------------------------------------

// Botones que cierran el diálogo en el que están
document.querySelectorAll("[data-cerrar]").forEach((boton) =>
  boton.addEventListener("click", () => boton.closest("dialog").close())
);

$("#reintentar").addEventListener("click", iniciar);

iniciar();
