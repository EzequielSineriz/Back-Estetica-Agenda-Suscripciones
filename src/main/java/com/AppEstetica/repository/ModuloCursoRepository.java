package com.AppEstetica.repository;

import com.AppEstetica.entities.ModuloCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloCursoRepository extends JpaRepository<ModuloCurso,Long> {

    List<ModuloCurso> findByCursoIdOrderByOrdenAsc(Long cursoId);
}
