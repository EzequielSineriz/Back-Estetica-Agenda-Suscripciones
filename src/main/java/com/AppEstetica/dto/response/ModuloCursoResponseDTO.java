package com.AppEstetica.dto.response;

public record ModuloCursoResponseDTO(
        Long id,
        Integer orden,
        String titulo,
        String descripcion,
        String videoUrl,
        String pdfUrl
) {
}
