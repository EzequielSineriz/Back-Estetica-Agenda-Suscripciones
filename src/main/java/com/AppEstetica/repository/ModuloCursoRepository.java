package com.AppEstetica.repository;

import com.AppEstetica.entities.ModuloCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuloCursoRepository extends JpaRepository<ModuloCurso,Long> {

    List<ModuloCurso> findByCursoIdOrderByOrdenAsc(Long cursoId);
}
