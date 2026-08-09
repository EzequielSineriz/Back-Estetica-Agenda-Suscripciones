package com.AppEstetica.repository;


import com.AppEstetica.entities.SesionTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionTratamientoRepository extends JpaRepository<SesionTratamiento, Long> {
    List<SesionTratamiento> findByClientIdOrderByFechaDesc(Long clientId);
}
