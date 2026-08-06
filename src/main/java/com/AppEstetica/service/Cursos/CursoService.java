package com.AppEstetica.service.Cursos;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.entities.Curso;
import com.AppEstetica.repository.CursoRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CursoService implements ICursoService {

    private final CursoRespository cursoRespository;


    @Override
    public List<Curso> getDisponibles() {
       return cursoRespository.findByActivoTrue();
    }

    @Override
    public Curso crear(CursoRequestDTO dto) {
        return cursoRespository.save(Curso.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .precio(dto.precio())
                .activo(true)
                .build());
    }
}
