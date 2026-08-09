package com.AppEstetica.dto.request;

public record FichaClinicaRequestDTO(
        String tipoPiel,
        String alergias,
        String antecedentesMedicos,
        String observacionesGenerales,
        Boolean TomaMedicacion
) {}