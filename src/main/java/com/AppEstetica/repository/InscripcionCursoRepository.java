package com.AppEstetica.repository;

import com.AppEstetica.entities.EstadoPago;
import com.AppEstetica.entities.InscripcionCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionCursoRepository extends JpaRepository<InscripcionCurso,Long> {
    Optional<InscripcionCurso> findByMercadoPagoPaymentId(String paymentId);
    List<InscripcionCurso> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndCursoIdAndEstado(Long usuarioId, Long cursoId, EstadoPago estado); // 👈 nuevo

}
