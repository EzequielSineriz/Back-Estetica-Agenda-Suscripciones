package com.AppEstetica.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fichas_clinicas")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FichaClinica extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    private String tipoPiel; // ej: "Grasa", "Mixta", "Seca", "Sensible"

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String antecedentesMedicos;

    @Column(columnDefinition = "TEXT")
    private String observacionesGenerales;

    private Boolean tomaMedicacion;
}