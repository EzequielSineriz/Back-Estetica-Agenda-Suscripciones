package com.AppEstetica.repository;

import com.AppEstetica.entities.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRespository extends JpaRepository<Curso,Long> {
    List<Curso> findByActivoTrue();
}
