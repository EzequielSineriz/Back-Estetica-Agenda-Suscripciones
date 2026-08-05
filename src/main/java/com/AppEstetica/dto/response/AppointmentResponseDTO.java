package com.AppEstetica.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentResponseDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private LocalDate date;
    private LocalTime time;
    private LocalTime endTime;
    private String avatarUrl;
    private String service;
    private String status;
}
