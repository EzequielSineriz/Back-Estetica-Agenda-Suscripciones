package com.AppEstetica.service.Turnos;

import com.AppEstetica.advice.BadRequestException;
import com.AppEstetica.advice.ConflictException;
import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.entities.Client;
import com.AppEstetica.repository.AppointmentRepository;
import com.AppEstetica.service.Cliente.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentService implements IAppointmentService {
    private final AppointmentRepository repo;
    private final ClientService clientService;

    public Page<Appointment> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Appointment getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("El turno con el " + id + " No encontrado"));

    }

    @Transactional
    public Appointment update(Long id, AppointmentRequestDTO dto) {
        Appointment existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + id));

        // Pasamos 'id' para excluir este turno de la validación al editar
        validarHorarioDisponible(dto.getDate(), dto.getTime(), dto.getEndTime(), id);

        BigDecimal precioTotal = dto.getPrecioTotal();
        BigDecimal sena = dto.getMontoSena() != null ?
                dto.getMontoSena() : precioTotal.multiply(new BigDecimal("0.10"));

        existing.setDate(dto.getDate());
        existing.setTime(dto.getTime());
        existing.setEndTime(dto.getEndTime());
        existing.setService(dto.getService());
        existing.setPrecioTotal(precioTotal);
        existing.setMontoSena(sena);
        existing.setClient(clientService.findById(dto.getClientId()));

        return repo.save(existing);
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
        validarHorarioDisponible(dto.getDate(), dto.getTime(), dto.getEndTime(), null);

        Client client = clientService.findById(dto.getClientId());

        BigDecimal precioTotal = dto.getPrecioTotal();
        BigDecimal sena = dto.getMontoSena() != null ?
                dto.getMontoSena() : precioTotal.multiply(new BigDecimal("0.30"));

        Appointment appointment = Appointment.builder()
                .date(dto.getDate())
                .time(dto.getTime())
                .endTime(dto.getEndTime())
                .service(dto.getService())
                .client(client)
                .precioTotal(precioTotal)
                .montoSena(sena)
                .montoPagado(BigDecimal.ZERO)
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        return repo.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId) {
        Appointment app = repo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + appointmentId));

        // Referenciamos el enum interno usando Appointment.AppointmentStatus
        if (app.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            app.getStatus(); // Opcional
            app.setStatus(Appointment.AppointmentStatus.COMPLETED);
            clientService.updateVisitStats(app.getClient().getId());
        }

        return repo.save(app);
    }


    private void validarHorarioDisponible(LocalDate date, LocalTime time, LocalTime endTime, Long excludeId) {
        if (!time.isBefore(endTime)) {
            throw new BadRequestException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Si excludeId es null, se verifica para la creación. Si tiene valor, se usa para edición.
        boolean overlaps = (excludeId == null)
                ? repo.existsOverlapping(date, time, endTime)
                : repo.existsOverlappingExcludingId(date, time, endTime, excludeId);

        if (overlaps) {
            throw new ConflictException("Ya existe un turno que se superpone con ese horario");
        }
    }


        @Transactional
        public void registrarPagoSena(Long appointmentId, BigDecimal montoAbonado) {
            Appointment appointment = repo.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + appointmentId));

            appointment.setMontoPagado(appointment.getMontoPagado().add(montoAbonado));

            // Si cubrió al menos el monto de la seña, pasa a RESERVED
            if (appointment.getMontoPagado().compareTo(appointment.getMontoSena()) >= 0) {
                appointment.setStatus(Appointment.AppointmentStatus.RESERVED);
            }

            // Si pagó el 100%, pasa a CONFIRMED
            if (appointment.getMontoPagado().compareTo(appointment.getPrecioTotal()) >= 0) {
                appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            }

            repo.save(appointment);
        }


    }
