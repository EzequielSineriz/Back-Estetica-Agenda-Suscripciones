package com.AppEstetica.service.Cursos;

import com.AppEstetica.dto.request.ModuloCursoRequestDTO;
import com.AppEstetica.entities.ModuloCurso;

import java.util.List;

public interface IModuloCursoService {
    List<ModuloCurso> getByCurso(Long cursoId);
    ModuloCurso crear(Long cursoId, ModuloCursoRequestDTO dto);
    ModuloCurso actualizar(Long moduloId, ModuloCursoRequestDTO dto);
    void eliminar(Long moduloId);
}
