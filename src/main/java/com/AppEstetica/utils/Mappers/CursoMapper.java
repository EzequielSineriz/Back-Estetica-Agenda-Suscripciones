package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.dto.response.CursoResponseDTO;
import com.AppEstetica.entities.Curso;

public class CursoMapper {
    public static Curso toEntity(CursoRequestDTO dto) {
        return Curso.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .precio(dto.precio())
                .activo(true)
                .build();
    }

    public static CursoResponseDTO toDTO(Curso entity) {
        return new CursoResponseDTO(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio()
        );
    }
}