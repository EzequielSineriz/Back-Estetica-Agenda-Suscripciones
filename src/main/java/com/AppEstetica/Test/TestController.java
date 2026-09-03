package com.AppEstetica.Test;

import com.AppEstetica.service.Notificaciones.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailNotificationService emailService;

    @PostMapping("/email-curso")
    public ResponseEntity<String> probarEmail(@RequestParam String emailDestino) {
        emailService.enviarConfirmacionCurso(
                emailDestino,
                "Willyberto",
                "Taller Hi-fu",
                1L
        );
        return ResponseEntity.ok("Email de prueba enviado a " + emailDestino);
    }
}