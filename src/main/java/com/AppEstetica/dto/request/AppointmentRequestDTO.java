package com.AppEstetica.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentRequestDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime time;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotBlank(message = "El servicio es obligatorio")
    private String service;

    private BigDecimal precioTotal;

    // Cambiado de @NotBlank a @NotNull o puede ser opcional
    private BigDecimal montoSena;
}
