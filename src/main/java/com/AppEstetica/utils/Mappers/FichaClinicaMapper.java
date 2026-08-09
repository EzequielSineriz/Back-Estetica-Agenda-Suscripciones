package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.FichaClinicaRequestDTO;
import com.AppEstetica.dto.response.FichaClinicaResponseDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.entities.FichaClinica;

public class FichaClinicaMapper {

    public static FichaClinica toEntity(FichaClinicaRequestDTO dto, Client client) {
        return FichaClinica.builder()
                .client(client)
                .tipoPiel(dto.tipoPiel())
                .alergias(dto.alergias())
                .antecedentesMedicos(dto.antecedentesMedicos())
                .observacionesGenerales(dto.observacionesGenerales())
                .tomaMedicacion(dto.TomaMedicacion())
                .build();
    }

    public static FichaClinicaResponseDTO toDTO(FichaClinica entity) {
        return new FichaClinicaResponseDTO(
                entity.getId(),
                entity.getClient().getId(),
                entity.getTipoPiel(),
                entity.getAlergias(),
                entity.getAntecedentesMedicos(),
                entity.getObservacionesGenerales(),
                entity.getTomaMedicacion()
        );
    }
}