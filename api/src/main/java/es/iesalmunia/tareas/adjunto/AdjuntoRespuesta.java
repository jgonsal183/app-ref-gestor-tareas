package es.iesalmunia.tareas.adjunto;

import java.time.Instant;

public record AdjuntoRespuesta(Long id, String nombre, String tipoContenido, long tamano, Instant subidoEn) {

    public static AdjuntoRespuesta de(Adjunto adjunto) {
        return new AdjuntoRespuesta(adjunto.getId(), adjunto.getNombre(), adjunto.getTipoContenido(),
                adjunto.getTamano(), adjunto.getSubidoEn());
    }
}
