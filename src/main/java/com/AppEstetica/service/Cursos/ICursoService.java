package com.AppEstetica.service.Cursos;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.entities.Curso;

import java.util.List;

public interface ICursoService {

    // Me va a decir cual esta activo
    List<Curso> getDisponibles();

    Curso crear(CursoRequestDTO dto);

    Curso getById(Long id);

}
