package com.AppEstetica.repository;

import com.AppEstetica.entities.FichaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaClinicaRepository extends JpaRepository<FichaClinica, Long> {
    Optional<FichaClinica> findByClientId(Long clientId);
}