package com.AppEstetica.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SesionTratamientoRequestDTO(
        Long appointmentId, // opcional
        @NotNull LocalDate fecha,
        String observaciones,
        String fotoAntesUrl,
        String fotoDespuesUrl
) {
}
