package com.AppEstetica.dto.response;

import java.math.BigDecimal;

public record CursoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio
) {
}
