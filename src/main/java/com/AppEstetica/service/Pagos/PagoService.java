package com.AppEstetica.service.Pagos;

import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.response.PreferenciaPagoResponseDTO;
import com.AppEstetica.entities.Curso;
import com.AppEstetica.entities.EstadoPago;
import com.AppEstetica.entities.InscripcionCurso;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.CursoRespository;
import com.AppEstetica.repository.InscripcionCursoRepository;
import com.AppEstetica.repository.UserRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final CursoRespository cursoRepository;
    private final InscripcionCursoRepository inscripcionRepository;
    private final UserRepository userRepository;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${mercadopago.backend-base-url}")
    private String backendBaseUrl;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public PreferenciaPagoResponseDTO crearPreferencia(Long cursoId, Long usuarioId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 1. Registramos la inscripción como PENDIENTE antes de ir a MercadoPago
        InscripcionCurso inscripcion = inscripcionRepository.save(InscripcionCurso.builder()
                .usuario(usuario)
                .curso(curso)
                .estado(EstadoPago.PENDIENTE)
                .fechaInscripcion(LocalDateTime.now())
                .build());

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(curso.getNombre())
                    .quantity(1)
                    .unitPrice(curso.getPrecio())
                    .currencyId("ARS")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendBaseUrl + "/cursos/pago-exitoso")
                    .pending(frontendBaseUrl + "/cursos/pago-pendiente")
                    .failure(frontendBaseUrl + "/cursos/pago-fallido")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(inscripcion.getId().toString()) // 👈 clave: así identificamos la inscripción en el webhook
                    .notificationUrl(backendBaseUrl + "/api/pagos/webhook")
                    .build();

            Preference preference = new PreferenceClient().create(request);

            return new PreferenciaPagoResponseDTO(preference.getInitPoint(), inscripcion.getId());

        } catch (MPApiException | MPException e) {
            throw new RuntimeException("Error creando preferencia de pago", e);
        }
    }

    // Llamado desde el webhook cuando MercadoPago notifica un cambio de estado
    public void procesarNotificacion(String paymentId) {
        try {
            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
            Long inscripcionId = Long.parseLong(payment.getExternalReference());

            InscripcionCurso inscripcion = inscripcionRepository.findById(inscripcionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada"));

            // Idempotencia: si ya procesamos este mismo pago, no lo repetimos
            if (payment.getId().toString().equals(inscripcion.getMercadoPagoPaymentId())
                    && inscripcion.getEstado() != EstadoPago.PENDIENTE) {
                return;
            }

            inscripcion.setMercadoPagoPaymentId(payment.getId().toString());
            inscripcion.setEstado(mapearEstado(payment.getStatus()));
            inscripcionRepository.save(inscripcion);

        } catch (MPApiException | MPException e) {
            throw new RuntimeException("Error procesando notificación de pago", e);
        }
    }

    private EstadoPago mapearEstado(String status) {
        return switch (status) {
            case "approved" -> EstadoPago.APROBADO;
            case "rejected" -> EstadoPago.RECHAZADO;
            default -> EstadoPago.PENDIENTE;
        };
    }
}