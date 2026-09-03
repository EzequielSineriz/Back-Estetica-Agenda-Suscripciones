package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.dto.response.CursoResponseDTO;
import com.AppEstetica.entities.Curso;

import java.util.Arrays;
import java.util.List;

public class CursoMapper {
    public static Curso toEntity(CursoRequestDTO dto) {
        return Curso.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .imagenUrl(dto.imagenUrl())
                .modalidad(dto.modalidad())
                .instructor(dto.instructor())
                .incluye(dto.incluye())
                .precio(dto.precio())
                .activo(true)
                .build();
    }

    public static CursoResponseDTO toDTO(Curso entity) {
        List<String> incluyeList = entity.getIncluye() != null
                ? Arrays.stream(entity.getIncluye().split("\n")).filter(s -> !s.isBlank()).toList()
                : List.of();

        return new CursoResponseDTO(
                entity.getId(), entity.getNombre(), entity.getDescripcion(),
                entity.getImagenUrl(), entity.getModalidad(), entity.getInstructor(), incluyeList, entity.getPrecio()
        );
    }
}