package com.AppEstetica.repository;

import com.AppEstetica.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.client c WHERE c.id = :clientId")
    List<Appointment> findByClientIdWithClient(@Param("clientId") Long clientId);
}
