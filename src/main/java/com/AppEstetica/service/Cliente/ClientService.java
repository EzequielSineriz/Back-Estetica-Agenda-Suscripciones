package com.AppEstetica.service.Cliente;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.repository.ClientRepository;
import com.AppEstetica.utils.Mappers.ClientMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ClientService implements IClientService {
    private final ClientRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    @Transactional
    public Client crear(ClientRequestDTO dto) {
        Client cliente = ClientMapper.toEntity(dto);

        if (cliente.getAvatarUrl() == null || cliente.getAvatarUrl().isBlank()) {
            cliente.setAvatarUrl("woman1");
        }

        return repo.save(cliente);
    }

    @Override
    @Transactional
    public Client update(Long id, ClientRequestDTO dto) {
        Client existingClient = findById(id);

        existingClient.setName(dto.getName());
        existingClient.setPhone(dto.getPhone());
        existingClient.setEmail(dto.getEmail());
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            existingClient.setAvatarUrl(dto.getAvatarUrl());
        }

        return repo.save(existingClient);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true) // 👈 Evita LazyInitializationException al mapear relaciones
    public Client findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void updateVisitStats(Long clientId) {
        Client client = findById(clientId);
        client.setTotalVisits(client.getTotalVisits() + 1);
        client.setLastVisit(LocalDateTime.now());
        repo.save(client);
    }
}