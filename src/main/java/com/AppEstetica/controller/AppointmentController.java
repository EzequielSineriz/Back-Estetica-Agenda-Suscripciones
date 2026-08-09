package com.AppEstetica.controller;


import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.dto.response.AppointmentResponseDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.service.Turnos.AppointmentService;
import com.AppEstetica.service.Cliente.ClientService;
import com.AppEstetica.utils.Mappers.AppointmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Clientes", description = "Gestión de clientes de la estética")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final ClientService clientService;

    @Operation(summary = "Listar todos los turnos (paginado)")
    @GetMapping
    public Page<AppointmentResponseDTO> getAppointments(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.getAll(pageable).map(AppointmentMapper::toDTO);
    }

    @GetMapping("/{id}")
    public AppointmentResponseDTO getAppointmentsId(
            @PathVariable Long id
    ){
        Appointment appointment = service.getById(id);
        AppointmentResponseDTO dto = AppointmentMapper.toDTO(appointment);
        return ResponseEntity.ok(dto).getBody();
    }

    @PostMapping
    public AppointmentResponseDTO createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        // Solo le pasamos el DTO al servicio, él sabe qué hacer.
        var savedAppointment = service.create(dto);
        return AppointmentMapper.toDTO(savedAppointment);
    }

    @PutMapping("/{id}")
    public AppointmentResponseDTO updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO dto) {

        return AppointmentMapper.toDTO(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/client/{clientId}")
    public List<AppointmentResponseDTO> getClientAppointments(
            @PathVariable Long clientId) {

        return service.getByClient(clientId)
                .stream()
                .map(AppointmentMapper::toDTO)
                .toList();
    }

    @PatchMapping("/{id}/complete")
    public AppointmentResponseDTO complete(@PathVariable Long id) {
        return AppointmentMapper.toDTO(service.completeAppointment(id));
    }

}