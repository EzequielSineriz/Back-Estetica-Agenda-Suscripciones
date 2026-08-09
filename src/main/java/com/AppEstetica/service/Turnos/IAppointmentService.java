package com.AppEstetica.service.Turnos;
import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface IAppointmentService {
    Page<Appointment> getAll(Pageable pageable);

    Appointment update(Long id, AppointmentRequestDTO dto);

    void delete(Long id);

    List<Appointment> getByClient(Long clientId);

    Appointment create(AppointmentRequestDTO dto);

    Appointment completeAppointment(Long appointmentId);
}
