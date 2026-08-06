package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.dto.response.AppointmentResponseDTO;
import com.AppEstetica.entities.Appointment;

public class AppointmentMapper {
    public static Appointment toEntity(AppointmentRequestDTO dto) {
        return Appointment.builder()
                .date(dto.getDate())
                .time(dto.getTime())
                .endTime(dto.getEndTime())
                .service(dto.getService())
                .build();
    }

    public static AppointmentResponseDTO toDTO(Appointment entity) {
        return AppointmentResponseDTO.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .time(entity.getTime())
                .endTime(entity.getEndTime())
                .service(entity.getService())
                .status(entity.getStatus().name())
                .clientId(
                        entity.getClient() != null
                                ? entity.getClient().getId()
                                : null
                )
                .clientName(
                        entity.getClient() != null
                                ? entity.getClient().getName()
                                : null
                )
                .avatarUrl(entity.getClient() != null ? entity.getClient().getAvatarUrl() : null)
                .build();
    }
}
