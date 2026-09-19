package es.iesalmunia.tareas.adjunto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    List<Adjunto> findByTareaIdOrderBySubidoEn(Long tareaId);
}
