package com.AppEstetica.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CursoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        String imagenUrl,
        String modalidad,
        String docente,
        List<String> incluye,  // lo partimos por línea al mapear, más cómodo para el front
        BigDecimal precio
) {}