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


    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.date = :date " +
            "AND a.time < :endTime AND a.endTime > :time " +
            "AND a.status <> com.AppEstetica.entities.Appointment.AppointmentStatus.CANCELLED") // 👈 Agregado .Appointment.
    boolean existsOverlapping(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.date = :date " +
            "AND a.time < :endTime AND a.endTime > :time " +
            "AND a.id <> :excludeId " +
            "AND a.status <> com.AppEstetica.entities.Appointment.AppointmentStatus.CANCELLED")
    boolean existsOverlappingExcludingId(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );


    @Query("SELECT a FROM Appointment a JOIN FETCH a.client " +
            "WHERE a.date = :fecha " +
            "AND a.status <> com.AppEstetica.entities.Appointment.AppointmentStatus.CANCELLED " +
            "AND a.recordatorioEnviado = false")
    List<Appointment> findTurnosParaRecordar(@Param("fecha") LocalDate fecha);
}
