package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
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

@DisplayName("Tests for BKG Confirmation Summaries Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGConfirmationSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGConfirmationSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  MockServerHttpResponse serverHttpResponse;
  MockServerHttpRequest serverHttpRequest;

  private final String BOOKING_CONFIRMATION_SUMMARIES_ENDPOINT = "/confirmed-booking-summaries";

  @Test
  @DisplayName("Get booking summaries should throw bad request for invalid document status.")
  void bookingConfirmationSummariesShouldThrowBadRequestForInvalidDocumentStatus() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(BOOKING_CONFIRMATION_SUMMARIES_ENDPOINT)
                    .queryParam("documentStatus", "DUMMY")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get booking summaries should throw bad request for limit 0.")
  void bookingConfirmationSummariesShouldThrowBadRequestForLimitZero() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(BOOKING_CONFIRMATION_SUMMARIES_ENDPOINT)
                    .queryParam("limit", "0")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName(
      "Get booking confirmation summaries should return valid list of booking request summaries for valid request.")
  void bookingConfirmationSummariesShouldReturnListOfBookingRequestSummaries() {

    String carrierBookingReference = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    BookingConfirmationSummaryTO bookingConfirmationSummaryTO = new BookingConfirmationSummaryTO();
    bookingConfirmationSummaryTO.setCarrierBookingReference(carrierBookingReference);
    bookingConfirmationSummaryTO.setConfirmationDateTime(dateTimeOffset);
    bookingConfirmationSummaryTO.setTermsAndConditions("TERMS AND CONDITIONS!");

    Mockito.when(bookingService.getBookingConfirmationSummaries(any(), any(), any()))
        .thenReturn(Flux.just(bookingConfirmationSummaryTO));

    webTestClient
        .get()
        .uri(BOOKING_CONFIRMATION_SUMMARIES_ENDPOINT)
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
        .isEqualTo(termsAndConditions)
    ;
  }
}
