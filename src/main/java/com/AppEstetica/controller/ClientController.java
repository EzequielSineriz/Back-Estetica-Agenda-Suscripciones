package com.AppEstetica.controller;

import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.dto.response.AppointmentResponseDTO;
import com.AppEstetica.dto.response.ClientResponseDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.service.Cliente.ClientService;
import com.AppEstetica.utils.Mappers.AppointmentMapper;
import com.AppEstetica.utils.Mappers.ClientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Clientes", description = "Administracion de clientes")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientController {
    private final ClientService service;


    @Operation(summary = "Listar todos los clientes (paginado)")
    @GetMapping
    public Page<ClientResponseDTO> getClients(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.getAll(pageable).map(ClientMapper::toDTO);
    }

    @Operation(summary = "Crear un cliente nuevo")
    @PostMapping
    public ClientResponseDTO createClient(@Valid @RequestBody ClientRequestDTO dto) {
        var client = ClientMapper.toEntity(dto);
        var saved = service.save(client);
        return ClientMapper.toDTO(saved);
    }

    @Operation(summary = "Actualizar los datos de un cliente")
    @PutMapping("/{id}")
    public ClientResponseDTO updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO dto) {
        // Le pasamos el ID y el DTO directamente al servicio
        var updated = service.update(id, dto);
        return ClientMapper.toDTO(updated);
    }

    @Operation(summary = "Elimina un cliente")
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

    @Operation(summary = "Obtener un cliente por id")
    @GetMapping("/{id}")
    public ClientResponseDTO getClientById(@PathVariable Long id){
        Client client = service.findById(id);
        return ClientMapper.toDTO(client);
    }
}
