package com.AppEstetica.service;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.entities.AppointmentStatus;
import com.AppEstetica.entities.Client;
import com.AppEstetica.repository.AppointmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repo;
    private final ClientService clientService;

    public List<Appointment> getAll() {
        return repo.findAll();
    }

    @Transactional
    public Appointment update(Long id, AppointmentRequestDTO dto) {
        Appointment existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        existing.setDate(dto.getDate());
        existing.setTime(dto.getTime());
        existing.setService(dto.getService());
        existing.setEndTime(dto.getEndTime());
        existing.setClient(clientService.findById(dto.getClientId()));

        return existing;
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByClient(Long clientId) {
        return repo.findByClientIdWithClient(clientId);
    }

    @Transactional
    public Appointment create(AppointmentRequestDTO dto) {
        Client client = clientService.findById(dto.getClientId());

        Appointment appointment = Appointment.builder()
                .date(dto.getDate())
                .time(dto.getTime())
                .endTime(dto.getEndTime())
                .service(dto.getService())
                .client(client)
                .status(AppointmentStatus.PENDING) // Forzamos estado inicial
                .build();

        return repo.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId) {
        Appointment app = repo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        // Evitamos duplicar stats si ya estaba completado
        if (app.getStatus() != AppointmentStatus.COMPLETED) {
            app.setStatus(AppointmentStatus.COMPLETED);
            clientService.updateVisitStats(app.getClient().getId());
        }

        return repo.save(app);
    }
}
