package com.AppEstetica.service.FichaClinica;

import com.AppEstetica.dto.request.FichaClinicaRequestDTO;
import com.AppEstetica.entities.FichaClinica;

public interface IFichaClinicaService {
    FichaClinica getByClient(Long clientId);

    FichaClinica guardar(Long clientId, FichaClinicaRequestDTO dto);

}
