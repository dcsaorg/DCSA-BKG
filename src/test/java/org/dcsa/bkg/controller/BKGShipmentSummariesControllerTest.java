package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.service.impl.ShipmentSummaryServiceImpl;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.time.OffsetDateTime;
import java.util.UUID;

import static bkg.config.TestConfig.validateAgainstJsonSchema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Tests for Shipment Summaries Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGShipmentSummariesController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGShipmentSummariesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean private ShipmentSummaryServiceImpl shipmentSummaryService;

  @MockBean private ExtendedParameters extendedParameters;

  @MockBean private R2dbcDialect r2dbcDialect;

  private final String SHIPMENT_SUMMARIES_ENDPOINT = "/shipment-summaries";

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
  @DisplayName(
      "Get shipment summaries should return valid list of booking request summaries for valid request.")
  void shipmentSummariesShouldReturnListOfBookingRequestSummaries() {

    String carrierBookingReference = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    Booking booking = new Booking();
    booking.setDocumentStatus(ShipmentEventTypeCode.RECE);
    booking.setCarrierBookingRequestReference("CBR1");

    ShipmentSummaryTO shipmentSummaryTO = new ShipmentSummaryTO();
    shipmentSummaryTO.setCarrierBookingReference(carrierBookingReference);
    shipmentSummaryTO.setConfirmationDateTime(dateTimeOffset);
    shipmentSummaryTO.setUpdatedDateTime(dateTimeOffset);
    shipmentSummaryTO.setTermsAndConditions(termsAndConditions);
    shipmentSummaryTO.setBooking(booking);

    Mockito.when(shipmentSummaryService.findAllExtended(any()))
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
        .isEqualTo(termsAndConditions)
        .consumeWith(
            entityExchangeResult -> {
              validateAgainstJsonSchema(entityExchangeResult, "shipmentSummary");
            });
  }

  @Test
  @DisplayName(
      "Get shipment summaries with documentStatusFilter should return valid list of booking request summaries for valid request.")
  void shipmentSummariesWithDocumentStatusFilterShouldReturnListOfBookingRequestSummaries() {

    String carrierBookingReference = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    Booking booking = new Booking();
    booking.setDocumentStatus(ShipmentEventTypeCode.RECE);
    booking.setCarrierBookingRequestReference("CBR1");

    ShipmentSummaryTO shipmentSummaryTO = new ShipmentSummaryTO();
    shipmentSummaryTO.setCarrierBookingReference(carrierBookingReference);
    shipmentSummaryTO.setConfirmationDateTime(dateTimeOffset);
    shipmentSummaryTO.setUpdatedDateTime(dateTimeOffset);
    shipmentSummaryTO.setTermsAndConditions(termsAndConditions);
    shipmentSummaryTO.setBooking(booking);

    Mockito.when(shipmentSummaryService.findAllExtended(any()))
        .thenReturn(Flux.just(shipmentSummaryTO));

    webTestClient
        .get()
        .uri(SHIPMENT_SUMMARIES_ENDPOINT + "?documentStatus=RECE")
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
        .consumeWith(
            entityExchangeResult -> {
              validateAgainstJsonSchema(entityExchangeResult, "shipmentSummary");
            });
  }
}
