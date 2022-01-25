package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Address;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tests for BKGController")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  private final String BOOKING_ENDPOINT = "/bookings";

  private BookingTO bookingTO;
  private BookingResponseTO bookingResponseTO;
  private BookingCancellationRequestTO bookingCancellationRequestTO;

  @BeforeEach
  void init() {
    // populate DTO with relevant objects to verify json schema returned
    bookingTO = new BookingTO();
    bookingTO.setCarrierBookingRequestReference(UUID.randomUUID().toString());
    bookingTO.setDocumentStatus(ShipmentEventTypeCode.PENU);
    bookingTO.setBookingRequestCreatedDateTime(OffsetDateTime.now());
    bookingTO.setReceiptTypeAtOrigin(ReceiptDeliveryType.CY);
    bookingTO.setDeliveryTypeAtDestination(ReceiptDeliveryType.SD);
    bookingTO.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
    bookingTO.setCargoMovementTypeAtDestination(CargoMovementType.LCL);
    bookingTO.setServiceContractReference("x".repeat(30));
    bookingTO.setCommunicationChannel(CommunicationChannel.AO);
    bookingTO.setSubmissionDateTime(OffsetDateTime.now());
    bookingTO.setExpectedDepartureDate(LocalDate.now());
    bookingTO.setInvoicePayableAt(new LocationTO());

    bookingTO.setIsExportDeclarationRequired(true);
    bookingTO.setIsPartialLoadAllowed(true);
    bookingTO.setIsImportLicenseRequired(true);
    bookingTO.setIsEquipmentSubstitutionAllowed(false);

    CommodityTO commodityTO = new CommodityTO();
    commodityTO.setCommodityType("x".repeat(20));
    commodityTO.setHsCode("x".repeat(10));
    commodityTO.setCargoGrossWeight(12.12);
    commodityTO.setCargoGrossWeightUnit(CargoGrossWeight.KGM);
    bookingTO.setCommodities(Collections.singletonList(commodityTO));

    ValueAddedServiceRequestTO valueAddedServiceRequestTO = new ValueAddedServiceRequestTO();
    valueAddedServiceRequestTO.setValueAddedServiceCode(ValueAddedServiceCode.CDECL);
    bookingTO.setValueAddedServiceRequests(Collections.singletonList(valueAddedServiceRequestTO));

    ReferenceTO referenceTO = new ReferenceTO();
    referenceTO.setReferenceType(ReferenceTypeCode.FF);
    referenceTO.setReferenceValue("x".repeat(100));
    bookingTO.setReferences(Collections.singletonList(referenceTO));

    RequestedEquipmentTO requestedEquipmentTO = new RequestedEquipmentTO();
    requestedEquipmentTO.setRequestedEquipmentSizetype("x".repeat(4));
    requestedEquipmentTO.setRequestedEquipmentUnits((int) (Math.random() * 100));
    requestedEquipmentTO.setShipperOwned(true);
    bookingTO.setRequestedEquipments(Collections.singletonList(requestedEquipmentTO));

    DocumentPartyTO documentPartyTO = new DocumentPartyTO();
    PartyTO partyTO = new PartyTO();
    partyTO.setIdentifyingCodes(
        Collections.singletonList(PartyTO.IdentifyingCode.builder().build()));
    partyTO.setAddress(new Address());
    partyTO.setPartyContactDetails(Collections.singletonList(new PartyContactDetailsTO()));
    documentPartyTO.setParty(partyTO);
    documentPartyTO.setPartyFunction(PartyFunction.N1);
    documentPartyTO.setDisplayedAddress(Collections.singletonList("x".repeat(250)));
    documentPartyTO.setIsToBeNotified(true);
    bookingTO.setDocumentParties(Collections.singletonList(documentPartyTO));

    LocationTO location = new LocationTO();
    location.setId("x".repeat(100));

    ShipmentLocationTO shipmentLocationTO = new ShipmentLocationTO();
    shipmentLocationTO.setLocation(location);
    shipmentLocationTO.setShipmentLocationTypeCode(LocationType.DRL);
    shipmentLocationTO.setDisplayedName("x".repeat(250));
    bookingTO.setShipmentLocations(Collections.singletonList(shipmentLocationTO));

    bookingResponseTO = new BookingResponseTO();
    bookingResponseTO.setCarrierBookingRequestReference(bookingTO.getCarrierBookingRequestReference());
    bookingResponseTO.setDocumentStatus(bookingTO.getDocumentStatus());
    bookingResponseTO.setBookingRequestCreatedDateTime(bookingTO.getBookingRequestCreatedDateTime());
    bookingResponseTO.setBookingRequestUpdatedDateTime(bookingTO.getBookingRequestUpdatedDateTime());

    bookingCancellationRequestTO = new BookingCancellationRequestTO();
    bookingCancellationRequestTO.setDocumentStatus(ShipmentEventTypeCode.CANC);
    bookingCancellationRequestTO.setReason("Booking Cancelled");
  }

  @Test
  @DisplayName("POST booking should return 202 and valid booking json schema.")
  void postBookingsShouldReturn202ForValidBookingRequest() {

    ArgumentCaptor<BookingTO> argument = ArgumentCaptor.forClass(BookingTO.class);

    // mock service method call
    when(bookingService.createBooking(any())).thenReturn(Mono.just(bookingResponseTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(BOOKING_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(bookingTO))
            .exchange();

    // these values are only allowed in response and not to be set via request body
    verify(bookingService).createBooking(argument.capture());

    // CarrierBookingRequestReference is set to null in the service implementation, as we need to be
    // able to set it via request in PUT
    assertNull(argument.getValue().getDocumentStatus());
    assertNull(argument.getValue().getBookingRequestCreatedDateTime());

    checkStatus202.andThen(checkBookingResponseTOJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("POST booking should return 400 for invalid request.")
  void postBookingsShouldReturn400ForInValidBookingRequest() {

    BookingTO invalidBookingTO = new BookingTO();

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .post()
            .uri(BOOKING_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidBookingTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName(
      "PUT booking should return 202 and valid booking json schema for given carrierBookingRequestReference.")
  void putBookingsShouldReturn202ForValidBookingRequest() {

    ArgumentCaptor<BookingTO> argument = ArgumentCaptor.forClass(BookingTO.class);

    // mock service method call
    when(bookingService.updateBookingByReferenceCarrierBookingRequestReference(any(), any()))
            .thenReturn(Mono.just(bookingResponseTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .put()
            .uri(BOOKING_ENDPOINT + "/" + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(bookingTO))
            .exchange();

    // these values are only allowed in response and not to be set via request body
    verify(bookingService).updateBookingByReferenceCarrierBookingRequestReference(any(), argument.capture());
    // CarrierBookingRequestReference is set to null in the service implementation, as we need to be
    // able to set it via request in PUT
    assertNull(argument.getValue().getDocumentStatus());
    assertNull(argument.getValue().getBookingRequestCreatedDateTime());

    checkStatus200.andThen(checkBookingResponseTOJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("PUT booking should return 400 for invalid request.")
  void putBookingsShouldReturn400ForInValidBookingRequest() {

    BookingTO invalidBookingTO = new BookingTO();

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .put()
            .uri(BOOKING_ENDPOINT + "/" + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidBookingTO))
            .exchange();

    checkStatus400.apply(exchange);
  }

  @Test
  @DisplayName(
      "GET booking should return 200 and valid json schema for given carrierBookingRequestReference.")
  void getBookingsShouldReturn200ForValidBookingAcknowledgementReference() {

    // mock service method call
    when(bookingService.getBookingByCarrierBookingRequestReference(any()))
        .thenReturn(Mono.just(bookingTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(BOOKING_ENDPOINT + "/" + UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.andThen(checkBookingResponseJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("Canceling a booking request should return a 200")
  void confirmedBookingsCancellationShouldReturn200() {

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .patch()
            .uri(
                BOOKING_ENDPOINT
                    + "/"
                    + bookingTO.getCarrierBookingRequestReference())
            .body(BodyInserters.fromValue(bookingCancellationRequestTO))
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.apply(exchange);
  }

  @Test
  @DisplayName(
      "Cancelling of a booking in unallowed status should return a 400 for invalid request")
  void confirmedBookingsCancellationShouldReturn400() {

    Mockito.when(
            bookingService.cancelBookingByCarrierBookingReference(
                bookingTO.getCarrierBookingRequestReference(), bookingCancellationRequestTO))
        .thenReturn(
            Mono.error(
                new UpdateException(
                    "Cannot Cancel Booking that is not in status RECE, PENU or CONF")));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .patch()
            .uri(
                BOOKING_ENDPOINT
                    + "/"
                    + bookingTO.getCarrierBookingRequestReference())
            .body(BodyInserters.fromValue(bookingCancellationRequestTO))
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus400.apply(exchange);
  }

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus200 =
      (exchange) -> exchange.expectStatus().isOk();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus202 =
      (exchange) -> exchange.expectStatus().isAccepted();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus204 =
      (exchange) -> exchange.expectStatus().isNoContent();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus400 =
      (exchange) -> exchange.expectStatus().isBadRequest();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec>
      checkBookingResponseJsonSchema =
          (exchange) ->
              exchange
                  .expectBody()
                  .consumeWith(System.out::println)
                  .jsonPath("$.carrierBookingRequestReference")
                  .hasJsonPath()
                  .jsonPath("$.documentStatus")
                  .hasJsonPath()
                  .jsonPath("$.bookingRequestCreatedDateTime")
                  .hasJsonPath()
                  .jsonPath("$.receiptTypeAtOrigin")
                  .hasJsonPath()
                  .jsonPath("$.deliveryTypeAtDestination")
                  .hasJsonPath()
                  .jsonPath("$.cargoMovementTypeAtOrigin")
                  .hasJsonPath()
                  .jsonPath("$.cargoMovementTypeAtDestination")
                  .hasJsonPath()
                  .jsonPath("$.serviceContractReference")
                  .hasJsonPath()
                  .jsonPath("$.paymentTermCode")
                  .hasJsonPath()
                  .jsonPath("$.isPartialLoadAllowed")
                  .hasJsonPath()
                  .jsonPath("$.isExportDeclarationRequired")
                  .hasJsonPath()
                  .jsonPath("$.exportDeclarationReference")
                  .hasJsonPath()
                  .jsonPath("$.isImportLicenseRequired")
                  .hasJsonPath()
                  .jsonPath("$.importLicenseReference")
                  .hasJsonPath()
                  .jsonPath("$.submissionDateTime")
                  .hasJsonPath()
                  .jsonPath("$.isAMSACIFilingRequired")
                  .hasJsonPath()
                  .jsonPath("$.isDestinationFilingRequired")
                  .hasJsonPath()
                  .jsonPath("$.contractQuotationReference")
                  .hasJsonPath()
                  .jsonPath("$.expectedDepartureDate")
                  .hasJsonPath()
                  .jsonPath("$.transportDocumentTypeCode")
                  .hasJsonPath()
                  .jsonPath("$.transportDocumentReference")
                  .hasJsonPath()
                  .jsonPath("$.bookingChannelReference")
                  .hasJsonPath()
                  .jsonPath("$.incoTerms")
                  .hasJsonPath()
                  .jsonPath("$.invoicePayableAt")
                  .hasJsonPath()
                  .jsonPath("$.placeOfIssue")
                  .hasJsonPath()
                  .jsonPath("$.communicationChannel")
                  .hasJsonPath()
                  .jsonPath("$.isEquipmentSubstitutionAllowed")
                  .hasJsonPath()
                  .jsonPath("$.vesselName")
                  .hasJsonPath()
                  .jsonPath("$.vesselIMONumber")
                  .hasJsonPath()
                  .jsonPath("$.exportVoyageNumber")
                  .hasJsonPath()
                  .jsonPath("$.commodities")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].commodityType")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].HSCode")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].cargoGrossWeight")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].cargoGrossWeightUnit")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].exportLicenseIssueDate")
                  .hasJsonPath()
                  .jsonPath("$.commodities[0].exportLicenseExpiryDate")
                  .hasJsonPath()
                  .jsonPath("$.valueAddedServiceRequests")
                  .hasJsonPath()
                  .jsonPath("$.valueAddedServiceRequests[0].valueAddedServiceCode")
                  .hasJsonPath()
                  .jsonPath("$.references")
                  .hasJsonPath()
                  .jsonPath("$.references[0].referenceType")
                  .hasJsonPath()
                  .jsonPath("$.references[0].referenceValue")
                  .hasJsonPath()
                  .jsonPath("$.requestedEquipments")
                  .hasJsonPath()
                  .jsonPath("$.requestedEquipments[0].requestedEquipmentSizetype")
                  .hasJsonPath()
                  .jsonPath("$.requestedEquipments[0].requestedEquipmentUnits")
                  .hasJsonPath()
                  .jsonPath("$.requestedEquipments[0].isShipperOwned")
                  .hasJsonPath()
                  .jsonPath("$.documentParties")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.partyName")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.taxReference1")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.taxReference2")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.publicKey")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.name")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.street")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.streetNumber")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.floor")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.postCode")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.city")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.stateRegion")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.address.country")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].partyFunction")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].displayedAddress")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.partyContactDetails")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.partyContactDetails[0].name")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.partyContactDetails[0].phone")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].party.partyContactDetails[0].email")
                  .hasJsonPath()
                  .jsonPath("$.documentParties[0].isToBeNotified")
                  .hasJsonPath()
                  .jsonPath("$.shipmentLocations")
                  .hasJsonPath();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec>
      checkBookingResponseTOJsonSchema =
          (exchange) ->
              exchange
                  .expectBody()
                  .consumeWith(System.out::println)
                  .jsonPath("$.carrierBookingRequestReference")
                  .hasJsonPath()
                  .jsonPath("$.documentStatus")
                  .hasJsonPath()
                  .jsonPath("$.bookingRequestCreatedDateTime")
                  .hasJsonPath()
                  .jsonPath("$.bookingRequestUpdatedDateTime")
                  .hasJsonPath();
}
