package com.AppEstetica.controller;

import com.AppEstetica.dto.request.FichaClinicaRequestDTO;
import com.AppEstetica.dto.request.SesionTratamientoRequestDTO;
import com.AppEstetica.dto.response.FichaClinicaResponseDTO;
import com.AppEstetica.dto.response.SesionTratamientoResponseDTO;
import com.AppEstetica.service.FichaClinica.FichaClinicaService;
import com.AppEstetica.service.SesionTratamiento.SesionTratamientoService;
import com.AppEstetica.utils.Mappers.FichaClinicaMapper;
import com.AppEstetica.utils.Mappers.SesionTratamientoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Historial clínico", description = "Ficha clínica y sesiones de tratamiento por cliente")
@PreAuthorize("hasRole('ADMIN')") // toda la historia clínica es información interna, nunca la ve un CUSTOMER
@RestController
@RequestMapping("/api/clients/{clientId}")
@RequiredArgsConstructor
public class FichaClinicaController {

    private final FichaClinicaService fichaService;
    private final SesionTratamientoService sesionService;

    @Operation(summary = "Obtener la ficha clínica de un cliente")
    @GetMapping("/ficha-clinica")
    public FichaClinicaResponseDTO getFicha(@PathVariable Long clientId) {
        return FichaClinicaMapper.toDTO(fichaService.getByClient(clientId));
    }

    @Operation(summary = "Crear o actualizar la ficha clínica de un cliente",
            description = "Si no existe, la crea. Si ya existe, la actualiza.")
    @PutMapping("/ficha-clinica")
    public FichaClinicaResponseDTO guardarFicha(@PathVariable Long clientId,
                                                @Valid @RequestBody FichaClinicaRequestDTO dto) {
        return FichaClinicaMapper.toDTO(fichaService.guardar(clientId, dto));
    }

    @Operation(summary = "Listar las sesiones de tratamiento de un cliente", description = "Ordenadas de más reciente a más antigua")
    @GetMapping("/sesiones")
    public List<SesionTratamientoResponseDTO> getSesiones(@PathVariable Long clientId) {
        return sesionService.getByClient(clientId).stream().map(SesionTratamientoMapper::toDTO).toList();
    }

    @Operation(summary = "Registrar una nueva sesión de tratamiento")
    @PostMapping("/sesiones")
    public SesionTratamientoResponseDTO crearSesion(@PathVariable Long clientId,
                                                    @Valid @RequestBody SesionTratamientoRequestDTO dto) {
        return SesionTratamientoMapper.toDTO(sesionService.crear(clientId, dto));
    }

    @Operation(summary = "Editar una sesión de tratamiento existente")
    @PutMapping("/sesiones/{sesionId}")
    public SesionTratamientoResponseDTO actualizarSesion(@PathVariable Long clientId,
                                                         @PathVariable Long sesionId,
                                                         @Valid @RequestBody SesionTratamientoRequestDTO dto) {
        return SesionTratamientoMapper.toDTO(sesionService.actualizar(sesionId, dto));
    }

    @Operation(summary = "Eliminar una sesión de tratamiento")
    @DeleteMapping("/sesiones/{sesionId}")
    public void eliminarSesion(@PathVariable Long clientId, @PathVariable Long sesionId) {
        sesionService.eliminar(sesionId);
    }
}