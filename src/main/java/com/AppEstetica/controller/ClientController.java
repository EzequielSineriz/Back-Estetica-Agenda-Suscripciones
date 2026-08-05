package com.AppEstetica.controller;

import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.dto.response.AppointmentResponseDTO;
import com.AppEstetica.dto.response.ClientResponseDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.service.ClientService;
import com.AppEstetica.utils.AppointmentMapper;
import com.AppEstetica.utils.ClientMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientController {
    private final ClientService service;


    @GetMapping
    public List<ClientResponseDTO> getClients() {
        return service.getAll().stream()
                .map(ClientMapper::toDTO)
                .toList();
    }

    @PostMapping
    public ClientResponseDTO createClient(@Valid @RequestBody ClientRequestDTO dto) {
        var client = ClientMapper.toEntity(dto);
        var saved = service.save(client);
        return ClientMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public ClientResponseDTO updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO dto) {
        // Le pasamos el ID y el DTO directamente al servicio
        var updated = service.update(id, dto);
        return ClientMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/appointments")
    public List<AppointmentResponseDTO> getClientHistory(@PathVariable Long id) {
        Client client = service.findById(id);
        return client.getAppointments().stream()
                .map(AppointmentMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO getClientById(@PathVariable Long id){
        Client client = service.findById(id);
        return ClientMapper.toDTO(client);
    }
}
