package com.AppEstetica.service.Cliente;
import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IClientService {

    Page<Client> getAll(Pageable pageable);
    Client save(Client c);
    Client update(Long id, ClientRequestDTO dto);
    void delete(Long id);
    Client findById(Long id);
    void updateVisitStats(Long clientId);
}
