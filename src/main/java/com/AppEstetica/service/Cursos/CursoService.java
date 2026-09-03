package com.AppEstetica.service.Cursos;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.entities.Curso;
import com.AppEstetica.repository.CursoRespository;
import com.AppEstetica.utils.Mappers.CursoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CursoService implements ICursoService {

    private final CursoRespository cursoRespository;

    @Override
    public Curso getById(Long id) {
        return cursoRespository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
    }

    @Override
    public List<Curso> getDisponibles() {
       return cursoRespository.findByActivoTrue();
    }

    @Override
    public Curso crear(CursoRequestDTO dto) {
        Curso curso = CursoMapper.toEntity(dto);
        return cursoRespository.save(curso);
    }

}
