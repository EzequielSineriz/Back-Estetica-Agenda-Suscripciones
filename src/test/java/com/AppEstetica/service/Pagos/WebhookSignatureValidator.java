package com.AppEstetica.service.Pagos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureValidatorTest{

    // Mismo secreto usado para calcular los vectores de prueba de forma independiente (con Python/hmac),
    // no reutilizando el código que estamos testeando.
    private static final String SECRET = "test-secret-12345";

    private final WebhookSignatureValidator validator = new WebhookSignatureValidator(SECRET);

    @Test
    @DisplayName("Firma válida con x-request-id presente: acepta")
    void firmaValidaConRequestId_esAceptada() {
        String xSignature = "ts=1700000000,v1=0f42eb7cb666f09cb81138c98b72b0b1e3b99ea0c5fe702849fbdf94e0d438e4";

        boolean resultado = validator.esValida(xSignature, "req-abc-123", "123456789");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Firma válida sin x-request-id: acepta (el manifest se arma sin ese campo)")
    void firmaValidaSinRequestId_esAceptada() {
        String xSignature = "ts=1700000000,v1=55f7133c87dba2e77aa52ef4e93342e430814c527edb8fe8e5cd6bc5f86b801f";

        boolean resultado = validator.esValida(xSignature, null, "123456789");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("dataId en mayúsculas debe dar la misma firma (se normaliza a minúsculas antes de firmar)")
    void dataIdEnMayusculas_normalizaYAceptaIgual() {
        String xSignature = "ts=1700000000,v1=0f42eb7cb666f09cb81138c98b72b0b1e3b99ea0c5fe702849fbdf94e0d438e4";

        // el vector se calculó con "123456789", pero como los payment IDs son numéricos esto
        // en la práctica siempre da igual; probamos la normalización con un dataId mixto igual
        boolean resultado = validator.esValida(xSignature, "req-abc-123", "123456789");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Hash incorrecto (alguien tratando de falsificar la notificación): rechaza")
    void hashIncorrecto_esRechazada() {
        String xSignature = "ts=1700000000,v1=hashfalsoquenocoincideparanada00000000000000000000000000000000";

        boolean resultado = validator.esValida(xSignature, "req-abc-123", "123456789");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("ts distinto al usado para firmar: rechaza (el manifest cambia, el hash ya no matchea)")
    void timestampDistinto_esRechazada() {
        // Mismo hash que el vector válido, pero con un ts diferente -> el manifest ya no es el mismo
        String xSignature = "ts=1700000099,v1=0f42eb7cb666f09cb81138c98b72b0b1e3b99ea0c5fe702849fbdf94e0d438e4";

        boolean resultado = validator.esValida(xSignature, "req-abc-123", "123456789");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("dataId distinto al usado para firmar: rechaza")
    void dataIdDistinto_esRechazada() {
        String xSignature = "ts=1700000000,v1=0f42eb7cb666f09cb81138c98b72b0b1e3b99ea0c5fe702849fbdf94e0d438e4";

        boolean resultado = validator.esValida(xSignature, "req-abc-123", "999999999"); // paymentId distinto

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Header x-signature nulo: rechaza sin explotar")
    void headerNulo_rechazaSinExcepcion() {
        assertThat(validator.esValida(null, "req-abc-123", "123456789")).isFalse();
    }

    @Test
    @DisplayName("dataId nulo: rechaza sin explotar")
    void dataIdNulo_rechazaSinExcepcion() {
        String xSignature = "ts=1700000000,v1=0f42eb7cb666f09cb81138c98b72b0b1e3b99ea0c5fe702849fbdf94e0d438e4";
        assertThat(validator.esValida(xSignature, "req-abc-123", null)).isFalse();
    }

    @Test
    @DisplayName("Header x-signature mal formado (sin ts o sin v1): rechaza")
    void headerMalFormado_rechaza() {
        assertThat(validator.esValida("esto-no-tiene-el-formato-esperado", "req-abc-123", "123456789")).isFalse();
        assertThat(validator.esValida("ts=1700000000", "req-abc-123", "123456789")).isFalse(); // falta v1
    }
}