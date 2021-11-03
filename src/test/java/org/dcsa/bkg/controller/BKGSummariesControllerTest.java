package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@DisplayName("Tests for BKG Summaries Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  private final String BOOKING_SUMMARIES_ENDPOINT = "/booking-summaries";

  @Test
  @DisplayName("Get booking summaries should throw bad request for invalid document status.")
  void bookingRequestSummariesShouldThrowBadRequestForInvalidDocumentStatus() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(BOOKING_SUMMARIES_ENDPOINT)
                    .queryParam("documentStatus", "DUMMY")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get booking summaries should throw bad request for limit 0.")
  void bookingRequestSummariesShouldThrowBadRequestForLimitZero() {

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(BOOKING_SUMMARIES_ENDPOINT).queryParam("limit", "0").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName(
      "Get booking summaries should return valid list of booking request summaries for valid request.")
  void bookingRequestSummariesShouldReturnListOfBookingRequestSummaries() {

    UUID uuid = UUID.randomUUID();

    BookingSummaryTO bookingSummaryTo = new BookingSummaryTO();
    bookingSummaryTo.setBookingAcknowledgementID(uuid);
    bookingSummaryTo.setReceiptTypeAtOrigin(ReceiptDeliveryType.CY);
    bookingSummaryTo.setDeliveryTypeAtDestination(ReceiptDeliveryType.CY);
    bookingSummaryTo.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
    bookingSummaryTo.setCargoMovementTypeAtDestination(CargoMovementType.FCL);
    bookingSummaryTo.setBookingRequestDateTime(OffsetDateTime.now());
    bookingSummaryTo.setServiceContractReference("234ase3q4");
    bookingSummaryTo.setPaymentTerm(PaymentTerm.PRE);
    bookingSummaryTo.setCargoGrossWeightUnit(CargoGrossWeight.KGM);
    bookingSummaryTo.setPartialLoadAllowed(true);
    bookingSummaryTo.setExportDeclarationRequired(true);
    bookingSummaryTo.setExportDeclarationReference("ABC123123");
    bookingSummaryTo.setImportLicenseRequired(true);
    bookingSummaryTo.setImportLicenseReference("ABC123123");
    bookingSummaryTo.setSubmissionDateTime(OffsetDateTime.now());
    bookingSummaryTo.setAMSACIFilingRequired(true);
    bookingSummaryTo.setDestinationFilingRequired(true);
    bookingSummaryTo.setContractQuotationReference("DKK");
    bookingSummaryTo.setExpectedDepartureDate(LocalDate.now());
    bookingSummaryTo.setTransportDocumentType(TransportDocumentTypeCode.BOL);
    bookingSummaryTo.setTransportDocumentReference("ASV23142ASD");
    bookingSummaryTo.setBookingChannelReference("ABC12313");
    bookingSummaryTo.setIncoTerms(IncoTerms.FCA);
    bookingSummaryTo.setCommunicationChannel(CommunicationChannel.AO);
    bookingSummaryTo.setEquipmentSubstitutionAllowed(true);

    Mockito.when(bookingService.getBookingRequestSummaries()).thenReturn(Flux.just(bookingSummaryTo));

    webTestClient
        .get()
        .uri(BOOKING_SUMMARIES_ENDPOINT)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$")
        .isArray()
        .jsonPath("$.[0].bookingAcknowledgementID")
        .isEqualTo(uuid.toString());
  }
}
