package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.SesionTratamientoRequestDTO;
import com.AppEstetica.dto.response.SesionTratamientoResponseDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.entities.Client;
import com.AppEstetica.entities.SesionTratamiento;

public class SesionTratamientoMapper {

    public static SesionTratamiento toEntity(SesionTratamientoRequestDTO dto, Client client, Appointment appointment) {
        return SesionTratamiento.builder()
                .client(client)
                .appointment(appointment) // puede ser null
                .fecha(dto.fecha())
                .observaciones(dto.observaciones())
                .fotoAntesUrl(dto.fotoAntesUrl())
                .fotoDespuesUrl(dto.fotoDespuesUrl())
                .build();
    }

    public static SesionTratamientoResponseDTO toDTO(SesionTratamiento entity) {
        return new SesionTratamientoResponseDTO(
                entity.getId(),
                entity.getClient().getId(),
                entity.getAppointment() != null ? entity.getAppointment().getId() : null,
                entity.getFecha(),
                entity.getObservaciones(),
                entity.getFotoAntesUrl(),
                entity.getFotoDespuesUrl()
        );
    }
}
