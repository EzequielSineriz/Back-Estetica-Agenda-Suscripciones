package com.AppEstetica.service;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository repo;


    public List<Client> getAll() { return repo.findAll(); }
    public Client save(Client c) {
        if(c.getAvatarUrl() == null) c.setAvatarUrl("woman1");
        return repo.save(c); }

    public Client update(Long id, ClientRequestDTO dto) {
        Client existingClient = findById(id); // Buscamos el original con todos sus datos

        // Actualizamos solo lo que viene del DTO
        existingClient.setName(dto.getName());
        existingClient.setPhone(dto.getPhone());
        if(dto.getAvatarUrl() != null) {
            existingClient.setAvatarUrl(dto.getAvatarUrl());
        }

        return repo.save(existingClient);
    }
    public void delete(Long id) { repo.deleteById(id); }
    public Client findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id)); }

    @Transactional
    public void updateVisitStats(Long clientId) {
        Client client = findById(clientId);
        client.setTotalVisits(client.getTotalVisits() + 1);
        client.setLastVisit(LocalDateTime.now());
        repo.save(client);
    }
}
