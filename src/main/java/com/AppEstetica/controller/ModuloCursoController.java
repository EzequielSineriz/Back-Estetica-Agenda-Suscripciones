package com.AppEstetica.controller;

import com.AppEstetica.advice.ForbiddenException;
import com.AppEstetica.dto.request.ModuloCursoRequestDTO;
import com.AppEstetica.dto.response.ModuloCursoResponseDTO;
import com.AppEstetica.entities.EstadoPago;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.InscripcionCursoRepository;

import com.AppEstetica.service.Cursos.ModuloCursoService;
import com.AppEstetica.utils.Mappers.ModuloCursoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Módulos de curso", description = "Contenido (video/PDF) de cada curso, organizado en módulos")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ModuloCursoController {

    private final ModuloCursoService service;
    private final InscripcionCursoRepository inscripcionRepository;
    

    @Operation(summary = "Listar los módulos de un curso",
            description = "Solo visible para ADMIN o para quien ya pagó ese curso puntual")
    @GetMapping("/cursos/{cursoId}/modulos")
    public List<ModuloCursoResponseDTO> listar(@PathVariable Long cursoId, @AuthenticationPrincipal User usuario) {

        boolean esAdmin = usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            boolean tieneAcceso = inscripcionRepository
                    .existsByUsuarioIdAndCursoIdAndEstado(usuario.getId(), cursoId, EstadoPago.APROBADO);

            if (!tieneAcceso) {
                throw new ForbiddenException("Necesitás haber comprado este curso para ver su contenido");
            }
        }

        return service.getByCurso(cursoId).stream().map(ModuloCursoMapper::toDTO).toList();
    }

    @Operation(summary = "Editar un módulo", description = "Solo ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modulos/{moduloId}")
    public ModuloCursoResponseDTO actualizar(@PathVariable Long moduloId, @Valid @RequestBody ModuloCursoRequestDTO dto) {
        return ModuloCursoMapper.toDTO(service.actualizar(moduloId, dto));
    }

    @Operation(summary = "Eliminar un módulo", description = "Solo ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/modulos/{moduloId}")
    public void eliminar(@PathVariable Long moduloId) {
        service.eliminar(moduloId);
    }
}