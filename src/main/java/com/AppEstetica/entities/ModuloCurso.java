package com.AppEstetica.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modulos_curso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloCurso extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private Integer orden; // 1, 2, 3... para mostrarlos en secuencia

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "video_url")
    private String videoUrl; // link de YouTube/Vimeo no listado

    @Column(name = "pdf_url")
    private String pdfUrl; // link del storage (Cloudinary/Supabase)
}
