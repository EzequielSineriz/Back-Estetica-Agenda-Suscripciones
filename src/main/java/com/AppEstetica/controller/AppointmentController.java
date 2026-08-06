package com.AppEstetica.controller;


import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.dto.response.AppointmentResponseDTO;
import com.AppEstetica.service.Turnos.AppointmentService;
import com.AppEstetica.service.Cliente.ClientService;
import com.AppEstetica.utils.Mappers.AppointmentMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final ClientService clientService;

    @GetMapping
    public List<AppointmentResponseDTO> getAppointments() {
        return service.getAll().stream()
                .map(AppointmentMapper::toDTO)
                .toList();
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