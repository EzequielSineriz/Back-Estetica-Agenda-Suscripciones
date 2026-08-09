package com.AppEstetica.service.SesionTratamiento;

import com.AppEstetica.dto.request.SesionTratamientoRequestDTO;
import com.AppEstetica.entities.SesionTratamiento;

import java.util.List;

public interface ISesionTratamientoService {
    List<SesionTratamiento> getByClient(Long clientId);

    SesionTratamiento crear(Long clientId, SesionTratamientoRequestDTO dto);

    SesionTratamiento actualizar(Long sesionId, SesionTratamientoRequestDTO dto);

    void eliminar(Long sesionId);
}
