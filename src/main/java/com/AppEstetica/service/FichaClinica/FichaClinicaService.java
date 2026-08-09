package com.AppEstetica.service.FichaClinica;


import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.FichaClinicaRequestDTO;
import com.AppEstetica.entities.Client;
import com.AppEstetica.entities.FichaClinica;
import com.AppEstetica.repository.ClientRepository;
import com.AppEstetica.repository.FichaClinicaRepository;
import com.AppEstetica.utils.Mappers.FichaClinicaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FichaClinicaService implements IFichaClinicaService {

    private final FichaClinicaRepository repository;
    private final ClientRepository clientRepository;

    public FichaClinica getByClient(Long clientId) {
        return repository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Este cliente todavía no tiene ficha clínica cargada"));
    }

    // Crea si no existe, actualiza si ya existe -- así el front no tiene que saber cuál de los dos casos es
    public FichaClinica guardar(Long clientId, FichaClinicaRequestDTO dto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        FichaClinica ficha = repository.findByClientId(clientId)
                .orElseGet(() -> FichaClinicaMapper.toEntity(dto, client));

        ficha.setTipoPiel(dto.tipoPiel());
        ficha.setAlergias(dto.alergias());
        ficha.setAntecedentesMedicos(dto.antecedentesMedicos());
        ficha.setObservacionesGenerales(dto.observacionesGenerales());
        ficha.setTomaMedicacion(dto.TomaMedicacion());

        return repository.save(ficha);
    }
}

