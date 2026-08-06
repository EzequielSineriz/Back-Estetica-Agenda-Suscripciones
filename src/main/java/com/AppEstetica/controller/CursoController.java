package com.AppEstetica.controller;

import com.AppEstetica.dto.request.CursoRequestDTO;
import com.AppEstetica.dto.response.CursoResponseDTO;
import com.AppEstetica.dto.response.PreferenciaPagoResponseDTO;
import com.AppEstetica.entities.User;
import com.AppEstetica.service.Cursos.CursoService;
import com.AppEstetica.service.Pagos.PagoService;
import com.AppEstetica.utils.Mappers.CursoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {
    private final CursoService service;
    private final PagoService pagoService;

    @GetMapping
    public List<CursoResponseDTO> getDisponibles() {
        return service.getDisponibles().stream().map(CursoMapper::toDTO).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CursoResponseDTO crear(@Valid @RequestBody CursoRequestDTO dto) {
        return CursoMapper.toDTO(service.crear(dto));
    }

    @PostMapping("/{id}/inscribirse")
    public PreferenciaPagoResponseDTO inscribirse(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return pagoService.crearPreferencia(id, usuario.getId());
    }
}