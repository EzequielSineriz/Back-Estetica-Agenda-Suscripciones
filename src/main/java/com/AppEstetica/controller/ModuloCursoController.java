package com.AppEstetica.controller;

import com.AppEstetica.dto.request.ModuloCursoRequestDTO;
import com.AppEstetica.dto.response.ModuloCursoResponseDTO;
import com.AppEstetica.service.Cursos.IModuloCursoService;
import com.AppEstetica.utils.Mappers.ModuloCursoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Módulos de curso", description = "Contenido (video/PDF) de cada curso, organizado en módulos")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ModuloCursoController {

    private final IModuloCursoService service;

    @Operation(summary = "Listar los módulos de un curso")
    @GetMapping("/cursos/{cursoId}/modulos")
    public List<ModuloCursoResponseDTO> listar(@PathVariable Long cursoId) {
        return service.getByCurso(cursoId).stream().map(ModuloCursoMapper::toDTO).toList();
    }

    @Operation(summary = "Agregar un módulo a un curso", description = "Solo ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cursos/{cursoId}/modulos")
    public ModuloCursoResponseDTO crear(@PathVariable Long cursoId, @Valid @RequestBody ModuloCursoRequestDTO dto) {
        return ModuloCursoMapper.toDTO(service.crear(cursoId, dto));
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