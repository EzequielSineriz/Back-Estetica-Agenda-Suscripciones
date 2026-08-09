package com.AppEstetica.service.Turnos;

import com.AppEstetica.advice.BadRequestException;
import com.AppEstetica.advice.ConflictException;
import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.AppointmentRequestDTO;
import com.AppEstetica.entities.Appointment;
import com.AppEstetica.entities.Client;
import com.AppEstetica.repository.AppointmentRepository;
import com.AppEstetica.service.Cliente.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repo;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private AppointmentService service;

    private Client clienteDePrueba;
    private AppointmentRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        clienteDePrueba = Client.builder().id(1L).name("Cliente Test").build();

        dtoValido = AppointmentRequestDTO.builder()
                .clientId(1L)
                .date(LocalDate.of(2026, 9, 1))
                .time(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .service("Depilación láser")
                .precioTotal(new BigDecimal("10000"))
                .build();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Si el horario se solapa con otro turno activo, lanza ConflictException")
        void seSolapaConOtroTurno_lanzaConflictException() {
            when(repo.existsOverlapping(dtoValido.getDate(), dtoValido.getTime(), dtoValido.getEndTime()))
                    .thenReturn(true);

            assertThrows(ConflictException.class, () -> service.create(dtoValido));

            // Nunca debería intentar guardar si el horario está ocupado
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Si la hora de fin es anterior o igual a la de inicio, lanza BadRequestException")
        void horaFinAntesQueInicio_lanzaBadRequestException() {
            dtoValido.setEndTime(LocalTime.of(9, 0)); // fin antes que el inicio (10:00)

            assertThrows(BadRequestException.class, () -> service.create(dtoValido));

            verifyNoInteractions(repo); // ni siquiera debería llegar a consultar solapamiento
        }

        @Test
        @DisplayName("Turno válido sin montoSena explícito calcula automáticamente el 30% de seña")
        void sinMontoSena_calculaSenaPorDefecto() {
            when(repo.existsOverlapping(any(), any(), any())).thenReturn(false);
            when(clientService.findById(1L)).thenReturn(clienteDePrueba);
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            Appointment resultado = service.create(dtoValido);

            assertThat(resultado.getMontoSena()).isEqualByComparingTo(new BigDecimal("3000")); // 30% de 10000
            assertThat(resultado.getMontoPagado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resultado.getStatus()).isEqualTo(Appointment.AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("Turno válido con montoSena explícito respeta el valor dado, no lo recalcula")
        void conMontoSenaExplicito_respetaElValor() {
            dtoValido.setMontoSena(new BigDecimal("5000"));
            when(repo.existsOverlapping(any(), any(), any())).thenReturn(false);
            when(clientService.findById(1L)).thenReturn(clienteDePrueba);
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            Appointment resultado = service.create(dtoValido);

            assertThat(resultado.getMontoSena()).isEqualByComparingTo(new BigDecimal("5000"));
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Si el turno no existe, lanza ResourceNotFoundException")
        void turnoNoExiste_lanzaResourceNotFoundException() {
            when(repo.findById(99L)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.update(99L, dtoValido));
        }

        @Test
        @DisplayName("Al editar, el propio turno se excluye del chequeo de solapamiento")
        void alEditar_excluyeElPropioTurnoDelSolapamiento() {
            Appointment existente = Appointment.builder()
                    .id(5L)
                    .precioTotal(BigDecimal.TEN)
                    .montoSena(BigDecimal.ONE)
                    .montoPagado(BigDecimal.ZERO)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .build();

            when(repo.findById(5L)).thenReturn(java.util.Optional.of(existente));
            when(repo.existsOverlappingExcludingId(dtoValido.getDate(), dtoValido.getTime(), dtoValido.getEndTime(), 5L))
                    .thenReturn(false);
            when(clientService.findById(1L)).thenReturn(clienteDePrueba);
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            service.update(5L, dtoValido);

            // Confirma que se usó la variante "ExcludingId", no la genérica
            verify(repo).existsOverlappingExcludingId(any(), any(), any(), eq(5L));
            verify(repo, never()).existsOverlapping(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("completeAppointment()")
    class CompleteAppointment {

        @Test
        @DisplayName("Si ya estaba COMPLETED, no vuelve a actualizar las estadísticas del cliente")
        void yaCompletado_noActualizaStatsDeNuevo() {
            Client cliente = Client.builder().id(1L).build();
            Appointment yaCompletado = Appointment.builder()
                    .id(7L)
                    .client(cliente)
                    .status(Appointment.AppointmentStatus.COMPLETED)
                    .build();

            when(repo.findById(7L)).thenReturn(java.util.Optional.of(yaCompletado));
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            service.completeAppointment(7L);

            verify(clientService, never()).updateVisitStats(anyLong());
        }

        @Test
        @DisplayName("Al completar un turno PENDING, actualiza las estadísticas del cliente una vez")
        void turnoPendiente_alCompletarActualizaStats() {
            Client cliente = Client.builder().id(1L).build();
            Appointment pendiente = Appointment.builder()
                    .id(8L)
                    .client(cliente)
                    .status(Appointment.AppointmentStatus.RESERVED)
                    .build();

            when(repo.findById(8L)).thenReturn(java.util.Optional.of(pendiente));
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            Appointment resultado = service.completeAppointment(8L);

            assertThat(resultado.getStatus()).isEqualTo(Appointment.AppointmentStatus.COMPLETED);
            verify(clientService, times(1)).updateVisitStats(1L);
        }
    }

    @Nested
    @DisplayName("registrarPagoSena()")
    class RegistrarPagoSena {

        @Test
        @DisplayName("Al alcanzar el monto de la seña, el turno pasa a RESERVED")
        void alcanzaMontoSena_pasaAReserved() {
            Appointment turno = Appointment.builder()
                    .id(10L)
                    .precioTotal(new BigDecimal("10000"))
                    .montoSena(new BigDecimal("3000"))
                    .montoPagado(BigDecimal.ZERO)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .build();

            when(repo.findById(10L)).thenReturn(java.util.Optional.of(turno));
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            service.registrarPagoSena(10L, new BigDecimal("3000"));

            assertThat(turno.getStatus()).isEqualTo(Appointment.AppointmentStatus.RESERVED);
        }

        @Test
        @DisplayName("Al pagar el 100% del precio total, el turno pasa a CONFIRMED")
        void pagaElTotal_pasaAConfirmed() {
            Appointment turno = Appointment.builder()
                    .id(11L)
                    .precioTotal(new BigDecimal("10000"))
                    .montoSena(new BigDecimal("3000"))
                    .montoPagado(new BigDecimal("3000")) // ya había pagado la seña
                    .status(Appointment.AppointmentStatus.RESERVED)
                    .build();

            when(repo.findById(11L)).thenReturn(java.util.Optional.of(turno));
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            service.registrarPagoSena(11L, new BigDecimal("7000")); // completa el resto

            assertThat(turno.getMontoPagado()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(turno.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Un pago parcial que no llega a cubrir la seña no cambia el estado")
        void pagoParcialInsuficiente_noCambiaEstado() {
            Appointment turno = Appointment.builder()
                    .id(12L)
                    .precioTotal(new BigDecimal("10000"))
                    .montoSena(new BigDecimal("3000"))
                    .montoPagado(BigDecimal.ZERO)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .build();

            when(repo.findById(12L)).thenReturn(java.util.Optional.of(turno));
            when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

            service.registrarPagoSena(12L, new BigDecimal("1000")); // no alcanza la seña de 3000

            assertThat(turno.getStatus()).isEqualTo(Appointment.AppointmentStatus.PENDING);
        }
    }
}