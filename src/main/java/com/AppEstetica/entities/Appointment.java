package com.AppEstetica.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Appointment extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private LocalTime time;   // formato HH:mm
    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String service;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private BigDecimal precioTotal;

    @Column(nullable = false)
    private BigDecimal montoSena;

    @Column(nullable = false)
    private BigDecimal montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AppointmentStatus status;

    public enum AppointmentStatus {
        PENDING_PAYMENT, // Esperando el pago de la seña
        RESERVED,        // Seña pagada / Turno reservado
        CONFIRMED,       // Totalmente pagado o confirmado por admin
        CANCELLED,       // Cancelado
        COMPLETED        // Servicio realizado
    }

}
