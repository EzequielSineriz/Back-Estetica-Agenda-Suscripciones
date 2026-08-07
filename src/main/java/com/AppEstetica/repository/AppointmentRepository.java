package com.AppEstetica.repository;

import com.AppEstetica.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);


    @Query("SELECT a FROM Appointment a JOIN FETCH a.client c WHERE c.id = :clientId")
    List<Appointment> findByClientIdWithClient(@Param("clientId") Long clientId);


    // Se solapan si: mi inicio es antes de que termine el otro Y mi fin es después de que empiece el otro.
    // Excluye turnos cancelados (esos no ocupan horario) y, en un update, excluye el propio turno.
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Appointment a
            WHERE a.date = :date
              AND a.status <> com.AppEstetica.entities.AppointmentStatus.CANCELLED
              AND (:excludeId IS NULL OR a.id <> :excludeId)
              AND a.time < :endTime
              AND a.endTime > :time
            """)
    boolean existsOverlapping(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
