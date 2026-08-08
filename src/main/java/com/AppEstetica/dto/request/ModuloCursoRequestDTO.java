package com.AppEstetica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuloCursoRequestDTO(
        @NotNull Integer orden,
        @NotBlank String titulo,
        String descripcion,
        String videoUrl,
        String pdfUrl

) {
}
