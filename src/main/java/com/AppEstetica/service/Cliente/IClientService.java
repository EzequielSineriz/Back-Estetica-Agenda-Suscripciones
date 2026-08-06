package com.AppEstetica.service.Cliente;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.entities.Client;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface IClientService {

    List<Client> getAll();
    Client save(Client c);
    Client update(Long id, ClientRequestDTO dto);
    void delete(Long id);
    Client findById(Long id);
    void updateVisitStats(Long clientId);
}
