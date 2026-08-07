package com.AppEstetica.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// entities/InscripcionCurso.java
@Entity
@Table(name = "inscripciones_curso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionCurso extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado;

    @Column(name = "mp_payment_id")
    private String mercadoPagoPaymentId; // el ID que devuelve MercadoPago, clave para reconciliar el webhook

    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;
}
