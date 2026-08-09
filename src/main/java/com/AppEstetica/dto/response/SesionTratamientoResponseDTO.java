package com.AppEstetica.dto.response;

import java.time.LocalDate;

public record SesionTratamientoResponseDTO(
        Long id,
        Long clientId,
        Long appointmentId,
        LocalDate fecha,
        String observaciones,
        String fotoAntesUrl,
        String fotoDespuesUrl
) {
}
