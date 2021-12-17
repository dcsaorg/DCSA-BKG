package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@DisplayName("Tests for Shipment Summaries Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGShipmentSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGShipmentSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  MockServerHttpResponse serverHttpResponse;
  MockServerHttpRequest serverHttpRequest;

  private final String SHIPMENT_SUMMARIES_ENDPOINT = "/shipment-summaries";

  @Test
  @DisplayName("Get shipment summaries should throw bad request for invalid document status.")
  void shipmentSummariesShouldThrowBadRequestForInvalidDocumentStatus() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(SHIPMENT_SUMMARIES_ENDPOINT)
                    .queryParam("documentStatus", "DUMMY")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get shipment should throw bad request for limit 0.")
  void shipmentSummariesShouldThrowBadRequestForLimitZero() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(SHIPMENT_SUMMARIES_ENDPOINT).queryParam("limit", "0").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName(
      "Get shipment summaries should return valid list of booking request summaries for valid request.")
  void shipmentSummariesShouldReturnListOfBookingRequestSummaries() {

    String carrierBookingReference = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    ShipmentSummaryTO shipmentSummaryTO = new ShipmentSummaryTO();
    shipmentSummaryTO.setCarrierBookingReference(carrierBookingReference);
    shipmentSummaryTO.setConfirmationDateTime(dateTimeOffset);
    shipmentSummaryTO.setTermsAndConditions("TERMS AND CONDITIONS!");

    Mockito.when(bookingService.getShipmentSummaries(any(), any()))
        .thenReturn(Flux.just(shipmentSummaryTO));

    webTestClient
        .get()
        .uri(SHIPMENT_SUMMARIES_ENDPOINT)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$")
        .isArray()
        .jsonPath("$.[0].carrierBookingReference")
        .isEqualTo(carrierBookingReference)
        .jsonPath("$.[0].termsAndConditions")
        .isEqualTo(termsAndConditions);
  }
}
