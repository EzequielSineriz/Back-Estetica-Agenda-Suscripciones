package com.AppEstetica.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sesiones_tratamiento", indexes = {
        @Index(name = "idx_sesiones_client_id", columnList = "client_id")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SesionTratamiento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Opcional: si la sesión viene de un turno reservado en el sistema.
    // Nullable porque puede cargarse una sesión suelta (ej. tratamientos previos a usar el sistema).
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private String fotoAntesUrl;
    private String fotoDespuesUrl;
}