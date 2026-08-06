package com.AppEstetica.utils.Mappers;

import com.AppEstetica.dto.request.ClientRequestDTO;
import com.AppEstetica.dto.response.ClientResponseDTO;
import com.AppEstetica.entities.Client;

public class ClientMapper {
    public static Client toEntity(ClientRequestDTO dto) {
        Client c = new Client();
        c.setName(dto.getName());
        c.setPhone(dto.getPhone());
        c.setAvatarUrl(dto.getAvatarUrl());
        return c;
    }

    public static ClientResponseDTO toDTO(Client entity) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastVisit(entity.getLastVisit());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setTotalVisits(entity.getTotalVisits());
        return dto;
    }
}
