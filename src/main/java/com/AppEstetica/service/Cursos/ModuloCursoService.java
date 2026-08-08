package com.AppEstetica.service.Cursos;
import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.ModuloCursoRequestDTO;
import com.AppEstetica.entities.Curso;
import com.AppEstetica.entities.ModuloCurso;
import com.AppEstetica.repository.CursoRespository;
import com.AppEstetica.repository.ModuloCursoRepository;
import com.AppEstetica.utils.Mappers.ModuloCursoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ModuloCursoService implements IModuloCursoService{


    private final ModuloCursoRepository repository;
    private final CursoRespository cursoRepository;


    @Override
    public List<ModuloCurso> getByCurso(Long cursoId) {
        return repository.findByCursoIdOrderByOrdenAsc(cursoId);
    }

    @Override
    public ModuloCurso crear(Long cursoId, ModuloCursoRequestDTO dto) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        return repository.save(ModuloCursoMapper.toEntity(dto, curso));
    }

    @Override
    public ModuloCurso actualizar(Long moduloId, ModuloCursoRequestDTO dto) {
        ModuloCurso modulo = repository.findById(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado"));

        modulo.setOrden(dto.orden());
        modulo.setTitulo(dto.titulo());
        modulo.setDescripcion(dto.descripcion());
        modulo.setVideoUrl(dto.videoUrl());
        modulo.setPdfUrl(dto.pdfUrl());

        return repository.save(modulo);
    }

    @Override
    public void eliminar(Long moduloId) {
        if (!repository.existsById(moduloId)) {
            throw new ResourceNotFoundException("Módulo no encontrado");
        }
        repository.deleteById(moduloId);
    }


}
