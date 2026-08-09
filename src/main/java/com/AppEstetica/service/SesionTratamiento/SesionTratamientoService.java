package com.AppEstetica.service.SesionTratamiento;


import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.SesionTratamientoRequestDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.entities.Client;
import com.AppEstetica.entities.SesionTratamiento;
import com.AppEstetica.repository.AppointmentRepository;
import com.AppEstetica.repository.ClientRepository;
import com.AppEstetica.repository.SesionTratamientoRepository;
import com.AppEstetica.utils.Mappers.SesionTratamientoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionTratamientoService implements ISesionTratamientoService {

    private final SesionTratamientoRepository repository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;

    public List<SesionTratamiento> getByClient(Long clientId) {
        return repository.findByClientIdOrderByFechaDesc(clientId);
    }

    public SesionTratamiento crear(Long clientId, SesionTratamientoRequestDTO dto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Appointment appointment = null;
        if (dto.appointmentId() != null) {
            appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        }

        return repository.save(SesionTratamientoMapper.toEntity(dto, client, appointment));
    }

    public SesionTratamiento actualizar(Long sesionId, SesionTratamientoRequestDTO dto) {
        SesionTratamiento sesion = repository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        sesion.setFecha(dto.fecha());
        sesion.setObservaciones(dto.observaciones());
        sesion.setFotoAntesUrl(dto.fotoAntesUrl());
        sesion.setFotoDespuesUrl(dto.fotoDespuesUrl());

        if (dto.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
            sesion.setAppointment(appointment);
        }

        return repository.save(sesion);
    }

    public void eliminar(Long sesionId) {
        if (!repository.existsById(sesionId)) {
            throw new ResourceNotFoundException("Sesión no encontrada");
        }
        repository.deleteById(sesionId);
    }
}