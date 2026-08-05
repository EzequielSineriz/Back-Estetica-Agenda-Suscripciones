package com.AppEstetica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentRequestDTO {
    @NotNull
    private Long clientId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private LocalTime time;

    @NotBlank
    private String service;

}
