package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.enums.CutOffDateTimeCode;
import org.dcsa.bkg.model.enums.LocationType;
import org.dcsa.bkg.model.enums.TransportPlanStage;
import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.enums.DCSATransportType;
import org.dcsa.core.events.model.enums.PaymentTerm;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.BeforeAll;
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
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@DisplayName("Tests for BKG Confirmed Booking Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGConfirmedBookingsController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGConfirmedBookingsControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  private final String CONFIRMED_BOOKING_ENDPOINT = "/confirmed-bookings";

  @Test
  @DisplayName(
      "Get confirmed bookings should return valid list of booking request summaries for valid request.")
  void confirmedBookingsShouldReturnListOfBookingConfirmation() {

    String carrierBookingReferenceID = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    UUID addressID = UUID.randomUUID();
    UUID facilityID = UUID.randomUUID();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    Location location = new Location();
    location.setLocationName("Islands Brygge");
    location.setAddressID(addressID);
    location.setUnLocationCode("DK CPH");
    location.setFacilityID(facilityID);
    location.setLatitude("55.6675569");
    location.setLongitude("12.57705349");

    TransportTO transport = new TransportTO();
    transport.setTransportPlanStage(TransportPlanStage.MNC);
    transport.setModeOfTransport(DCSATransportType.VESSEL);
    transport.setLoadLocation(location);
    transport.setDischargeLocation(location);
    transport.setPlannedDepartureDate(dateTimeOffset);
    transport.setPlannedArrivalDate(dateTimeOffset);
    transport.setVesselName("VesselName");
    transport.setVesselIMONumber("1234567");
    transport.setCarrierVoyageNumber("CarrierVoyageNumber");

    ShipmentCutOffTimeTO shipmentCutOffTime = new ShipmentCutOffTimeTO();
    shipmentCutOffTime.setCutOffDateTime(dateTimeOffset);
    shipmentCutOffTime.setCutOffDateTimeCode(CutOffDateTimeCode.AFD);

    ShipmentLocationTO shipmentLocation = new ShipmentLocationTO();
    shipmentLocation.setLocation(location);
    shipmentLocation.setLocationType(LocationType.PRE);
    shipmentLocation.setDisplayedName("DisplayedName");
    shipmentLocation.setEventDateTime(dateTimeOffset);

    ConfirmedEquipmentTO confirmedEquipment = new ConfirmedEquipmentTO();
    confirmedEquipment.setConfirmedEquipmentUnits(12);
    confirmedEquipment.setConfirmedEquipmentSizeType("WHAT");

    ChargeTO charge = new ChargeTO();
    charge.setChargeType("ChargeType");
    charge.setCalculationBasis("CalculationBasis");
    charge.setCurrencyAmount(12);
    charge.setCurrencyCode("ABC");
    charge.setQuantity(12);
    charge.setIsUnderShippersResponsibility(PaymentTerm.PRE);
    charge.setUnitPrice(12);

    CarrierClauseTO carrierClause = new CarrierClauseTO();
    carrierClause.setClauseContent("ClauseContent");

    BookingConfirmationTO bookingConfirmationTO = new BookingConfirmationTO();
    bookingConfirmationTO.setCarrierBookingReferenceID(carrierBookingReferenceID);
    bookingConfirmationTO.setTermsAndConditions(termsAndConditions);
    bookingConfirmationTO.setPlaceOfIssue(location);
    bookingConfirmationTO.setTransports(List.of(transport));
    bookingConfirmationTO.setShipmentCutOffTimes(List.of(shipmentCutOffTime));
    bookingConfirmationTO.setShipmentLocations(List.of(shipmentLocation));
    bookingConfirmationTO.setConfirmedEquipments(List.of(confirmedEquipment));
    bookingConfirmationTO.setCharges(List.of(charge));
    bookingConfirmationTO.setCarrierClauses(List.of(carrierClause));

    Mockito.when(bookingService.getBooking(carrierBookingReferenceID))
        .thenReturn(Mono.just(bookingConfirmationTO));

    webTestClient
        .get()
        .uri(CONFIRMED_BOOKING_ENDPOINT + "/" + carrierBookingReferenceID)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.carrierBookingReferenceID")
        .isEqualTo(carrierBookingReferenceID)
        .jsonPath("$.placeOfIssue.locationName")
        .isEqualTo(location.getLocationName())
        .jsonPath("$.placeOfIssue.UNLocationCode")
        .isEqualTo(location.getUnLocationCode())
        .jsonPath("$.placeOfIssue.latitude")
        .isEqualTo(location.getLatitude())
        .jsonPath("$.placeOfIssue.longitude")
        .isEqualTo(location.getLongitude())
        .jsonPath("$.termsAndConditions")
        .isEqualTo(termsAndConditions)
        .jsonPath("$.carrierClauses.[0].clauseContent")
        .isEqualTo(carrierClause.getClauseContent())
        .jsonPath("$.transports.[0].transportPlanStage")
        .isEqualTo(transport.getTransportPlanStage().toString())
        .jsonPath("$.transports.[0].modeOfTransport")
        .isEqualTo(transport.getModeOfTransport().toString())
        .jsonPath("$.transports.[0].loadLocation")
        .isNotEmpty()
        .jsonPath("$.transports.[0].dischargeLocation")
        .isNotEmpty()
        .jsonPath("$.transports.[0].vesselName")
        .isEqualTo(transport.getVesselName())
        .jsonPath("$.transports.[0].vesselIMONumber")
        .isEqualTo(transport.getVesselIMONumber())
        .jsonPath("$.transports.[0].carrierVoyageNumber")
        .isEqualTo(transport.getCarrierVoyageNumber())
        .jsonPath("$.shipmentCutOffTimes.[0].cutOffDateTimeCode")
        .isEqualTo(shipmentCutOffTime.getCutOffDateTimeCode().toString())
        .jsonPath("$.shipmentLocations.[0].location")
        .isNotEmpty()
        .jsonPath("$.shipmentLocations.[0].locationType")
        .isEqualTo(shipmentLocation.getLocationType().toString())
        .jsonPath("$.shipmentLocations.[0].displayedName")
        .isEqualTo(shipmentLocation.getDisplayedName())
        .jsonPath("$.confirmedEquipments.[0].confirmedEquipmentUnits")
        .isEqualTo(confirmedEquipment.getConfirmedEquipmentUnits())
        .jsonPath("$.confirmedEquipments.[0].confirmedEquipmentSizeType")
        .isEqualTo(confirmedEquipment.getConfirmedEquipmentSizeType())
        .jsonPath("$.charges.[0].chargeType")
        .isEqualTo(charge.getChargeType())
        .jsonPath("$.charges.[0].calculationBasis")
        .isEqualTo(charge.getCalculationBasis())
        .jsonPath("$.charges.[0].currencyAmount")
        .isEqualTo(charge.getCurrencyAmount())
        .jsonPath("$.charges.[0].currencyCode")
        .isEqualTo(charge.getCurrencyCode())
        .jsonPath("$.charges.[0].quantity")
        .isEqualTo(charge.getQuantity())
        .jsonPath("$.charges.[0].isUnderShippersResponsibility")
        .isEqualTo(charge.getIsUnderShippersResponsibility().toString())
        .jsonPath("$.charges.[0].unitPrice")
        .isEqualTo(charge.getUnitPrice());
  }

  @Test
  @DisplayName(
      "Get confirmed bookings should return valid list of booking request summaries for valid request.")
  void confirmedBookingsCancellationShouldReturn204() {

    String carrierBookingReferenceID = UUID.randomUUID().toString().substring(0, 33);

    BookingConfirmationTO bookingConfirmationTO = new BookingConfirmationTO();
    bookingConfirmationTO.setCarrierBookingReferenceID(carrierBookingReferenceID);

    Mockito.when(bookingService.getBooking(carrierBookingReferenceID))
        .thenReturn(Mono.just(bookingConfirmationTO));

    webTestClient
        .post()
        .uri(CONFIRMED_BOOKING_ENDPOINT + "/" + carrierBookingReferenceID + "/cancelation")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .post()
        .uri(CONFIRMED_BOOKING_ENDPOINT + "/" + carrierBookingReferenceID + "/cancellation")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNoContent();
  }
}
