package com.AppEstetica.controller;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.dto.response.CursoResponseDTO;
import com.AppEstetica.dto.response.PreferenciaPagoResponseDTO;
import com.AppEstetica.entities.User;
import com.AppEstetica.service.Cursos.CursoService;
import com.AppEstetica.service.Pagos.PagoService;
import com.AppEstetica.utils.Mappers.CursoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cursos", description = "Catálogo de cursos e inscripción con pago vía MercadoPago")
@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {
    private final CursoService service;
    private final PagoService pagoService;

    @Operation(summary = "Lista los cursos disponibles", description = "Endpoint publico")
    @GetMapping
    public List<CursoResponseDTO> getDisponibles() {
        return service.getDisponibles().stream().map(CursoMapper::toDTO).toList();
    }

    @Operation(summary = "Crear un curso nuevo", description = "Solo ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CursoResponseDTO crear(@Valid @RequestBody CursoRequestDTO dto) {
        return CursoMapper.toDTO(service.crear(dto));
    }

    @Operation(summary = "Inscribirse a un curso",
            description = "Genera una preferencia de pago en MercadoPago y devuelve el link (initPoint) para completar el pago")
    @PostMapping("/{id}/inscribirse")
    public PreferenciaPagoResponseDTO inscribirse(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return pagoService.crearPreferencia(id, usuario.getId());
    }
}