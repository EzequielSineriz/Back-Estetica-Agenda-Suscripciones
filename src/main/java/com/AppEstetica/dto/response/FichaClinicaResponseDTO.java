package com.AppEstetica.dto.response;

public record FichaClinicaResponseDTO(
        Long id,
        Long clientId,
        String tipoPiel,
        String alergias,
        String antecedentesMedicos,
        String observacionesGenerales,
        Boolean tomaMedicacion
) {
}
