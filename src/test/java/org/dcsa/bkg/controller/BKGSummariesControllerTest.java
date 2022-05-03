package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.impl.BookingSummaryServiceImpl;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static bkg.config.TestConfig.validateAgainstJsonSchema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Tests for BKG Summaries Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean private BookingSummaryServiceImpl bookingSummaryService;

  @MockBean private ExtendedParameters extendedParameters;

  @MockBean private R2dbcDialect r2dbcDialect;

  private final String BOOKING_SUMMARIES_ENDPOINT = "/booking-summaries";

  @BeforeEach
  public void init() {
    when(extendedParameters.getSortParameterName()).thenReturn("sort");
    when(extendedParameters.getPaginationPageSizeName()).thenReturn("limit");
    when(extendedParameters.getPaginationCursorName()).thenReturn("cursor");
    when(extendedParameters.getIndexCursorName()).thenReturn("|Offset|");
    when(extendedParameters.getEnumSplit()).thenReturn(",");
    when(extendedParameters.getQueryParameterAttributeSeparator()).thenReturn(",");
    when(extendedParameters.getPaginationCurrentPageName()).thenReturn("Current-Page");
    when(extendedParameters.getPaginationFirstPageName()).thenReturn("First-Page");
    when(extendedParameters.getPaginationPreviousPageName()).thenReturn("Last-Page");
    when(extendedParameters.getPaginationNextPageName()).thenReturn("Next-Page");
    when(extendedParameters.getPaginationLastPageName()).thenReturn("Last-Page");

    when(r2dbcDialect.getBindMarkersFactory()).thenReturn(BindMarkersFactory.anonymous("?"));
  }

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
  @DisplayName(
      "Get booking summaries filtered by documentstatus should return valid list of booking request summaries with correct documentStatus")
  void bookingRequestSummariesByDocumentStatusShouldReturnBookingRequestSummaries() {
    UUID uuid = UUID.randomUUID();

    BookingSummaryTO bookingSummaryTo = new BookingSummaryTO();
    bookingSummaryTo.setCarrierBookingRequestReference(uuid.toString());
    bookingSummaryTo.setDocumentStatus(ShipmentEventTypeCode.PENU);
    bookingSummaryTo.setReceiptTypeAtOrigin(ReceiptDeliveryType.CY);
    bookingSummaryTo.setDeliveryTypeAtDestination(ReceiptDeliveryType.CY);
    bookingSummaryTo.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
    bookingSummaryTo.setCargoMovementTypeAtDestination(CargoMovementType.FCL);
    bookingSummaryTo.setBookingRequestCreatedDateTime(OffsetDateTime.now());
    bookingSummaryTo.setBookingRequestUpdatedDateTime(OffsetDateTime.now());
    bookingSummaryTo.setServiceContractReference("234ase3q4");
    bookingSummaryTo.setPaymentTermCode(PaymentTerm.PRE);
    bookingSummaryTo.setIsPartialLoadAllowed(true);
    bookingSummaryTo.setIsExportDeclarationRequired(true);
    bookingSummaryTo.setExportDeclarationReference("ABC123123");
    bookingSummaryTo.setIsImportLicenseRequired(true);
    bookingSummaryTo.setImportLicenseReference("ABC123123");
    bookingSummaryTo.setSubmissionDateTime(OffsetDateTime.now());
    bookingSummaryTo.setIsAMSACIFilingRequired(true);
    bookingSummaryTo.setIsDestinationFilingRequired(true);
    bookingSummaryTo.setContractQuotationReference("DKK");
    bookingSummaryTo.setExpectedDepartureDate(LocalDate.now());
    bookingSummaryTo.setTransportDocumentTypeCode(TransportDocumentTypeCode.BOL);
    bookingSummaryTo.setTransportDocumentReference("ASV23142ASD");
    bookingSummaryTo.setBookingChannelReference("ABC12313");
    bookingSummaryTo.setIncoTerms(IncoTerms.FCA);
    bookingSummaryTo.setCommunicationChannel(CommunicationChannel.AO);
    bookingSummaryTo.setIsEquipmentSubstitutionAllowed(true);

    when(bookingSummaryService.findAllExtended(any())).thenReturn(Flux.just(bookingSummaryTo));

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
        .jsonPath("$.[0].carrierBookingRequestReference")
        .isEqualTo(uuid.toString())
        .jsonPath("$.[0].documentStatus")
        .isEqualTo(ShipmentEventTypeCode.PENU.name())
        .consumeWith(
            entityExchangeResult -> {
              validateAgainstJsonSchema(entityExchangeResult, "bookingSummary");
            });
  }

  @Test
  @DisplayName(
      "Get booking summaries should return valid list of booking request summaries for valid request.")
  void bookingRequestSummariesShouldReturnListOfBookingRequestSummaries() {

    UUID uuid = UUID.randomUUID();

    BookingSummaryTO bookingSummaryTo = new BookingSummaryTO();
    bookingSummaryTo.setCarrierBookingRequestReference(uuid.toString());
    bookingSummaryTo.setReceiptTypeAtOrigin(ReceiptDeliveryType.CY);
    bookingSummaryTo.setDeliveryTypeAtDestination(ReceiptDeliveryType.CY);
    bookingSummaryTo.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
    bookingSummaryTo.setCargoMovementTypeAtDestination(CargoMovementType.FCL);
    bookingSummaryTo.setBookingRequestCreatedDateTime(OffsetDateTime.now());
    bookingSummaryTo.setServiceContractReference("234ase3q4");
    bookingSummaryTo.setPaymentTermCode(PaymentTerm.PRE);
    bookingSummaryTo.setIsPartialLoadAllowed(true);
    bookingSummaryTo.setIsExportDeclarationRequired(true);
    bookingSummaryTo.setExportDeclarationReference("ABC123123");
    bookingSummaryTo.setIsImportLicenseRequired(true);
    bookingSummaryTo.setImportLicenseReference("ABC123123");
    bookingSummaryTo.setSubmissionDateTime(OffsetDateTime.now());
    bookingSummaryTo.setIsAMSACIFilingRequired(true);
    bookingSummaryTo.setIsDestinationFilingRequired(true);
    bookingSummaryTo.setContractQuotationReference("DKK");
    bookingSummaryTo.setExpectedDepartureDate(LocalDate.now());
    bookingSummaryTo.setTransportDocumentTypeCode(TransportDocumentTypeCode.BOL);
    bookingSummaryTo.setTransportDocumentReference("ASV23142ASD");
    bookingSummaryTo.setBookingChannelReference("ABC12313");
    bookingSummaryTo.setIncoTerms(IncoTerms.FCA);
    bookingSummaryTo.setCommunicationChannel(CommunicationChannel.AO);
    bookingSummaryTo.setIsEquipmentSubstitutionAllowed(true);

    when(bookingSummaryService.findAllExtended(any())).thenReturn(Flux.just(bookingSummaryTo));

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
        .jsonPath("$.[0].carrierBookingRequestReference")
        .isEqualTo(uuid.toString());
  }
}
