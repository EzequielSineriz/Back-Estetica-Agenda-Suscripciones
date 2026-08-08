package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.ModuloCursoRequestDTO;
import com.AppEstetica.dto.response.ModuloCursoResponseDTO;
import com.AppEstetica.entities.Curso;
import com.AppEstetica.entities.ModuloCurso;

public class ModuloCursoMapper {

    public static ModuloCurso toEntity(ModuloCursoRequestDTO dto, Curso curso) {
        return ModuloCurso.builder()
                .curso(curso)
                .orden(dto.orden())
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .videoUrl(dto.videoUrl())
                .pdfUrl(dto.pdfUrl())
                .build();
    }

    public static ModuloCursoResponseDTO toDTO(ModuloCurso entity) {
        return new ModuloCursoResponseDTO(
                entity.getId(),
                entity.getOrden(),
                entity.getTitulo(),
                entity.getDescripcion(),
                entity.getVideoUrl(),
                entity.getPdfUrl()
        );
    }
}