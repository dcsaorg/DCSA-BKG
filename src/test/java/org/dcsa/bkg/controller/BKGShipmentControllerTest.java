package org.dcsa.bkg.controller;

import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.function.Function;

@DisplayName("Tests for BKG Shipment Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGShipmentController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGShipmentControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BookingService bookingService;

  private final String CONFIRMED_BOOKING_ENDPOINT = "/shipments";

  private ShipmentTO shipmentTO;

  @BeforeEach
  void init() {
    String carrierBookingReferenceID = UUID.randomUUID().toString().substring(0, 33);
    OffsetDateTime dateTimeOffset = OffsetDateTime.now();
    UUID addressID = UUID.randomUUID();
    UUID facilityID = UUID.randomUUID();
    String termsAndConditions = "TERMS AND CONDITIONS!";

    LocationTO location = new LocationTO();
    location.setLocationName("Islands Brygge");
    location.setAddressID(addressID);
    location.setUnLocationCode("DK CPH");
    location.setFacilityID(facilityID);
    location.setLatitude("55.6675569");
    location.setLongitude("12.57705349");

    TransportTO transport = new TransportTO();
    transport.setTransportPlanStage(TransportPlanStageCode.MNC);
    transport.setModeOfTransport(DCSATransportType.VESSEL);
    transport.setLoadLocation(location);
    transport.setDischargeLocation(location);
    transport.setPlannedDepartureDate(OffsetDateTime.now());
    transport.setPlannedArrivalDate(OffsetDateTime.now());
    transport.setVesselName("VesselName");
    transport.setVesselIMONumber("1234567");
    transport.setCarrierVoyageNumber("CarrierVoyageNumber");

    ShipmentCutOffTimeTO shipmentCutOffTime = new ShipmentCutOffTimeTO();
    shipmentCutOffTime.setCutOffDateTime(dateTimeOffset);
    shipmentCutOffTime.setCutOffDateTimeCode(CutOffDateTimeCode.AFD);

    ShipmentLocationTO shipmentLocation = new ShipmentLocationTO();
    shipmentLocation.setLocation(location);
    shipmentLocation.setShipmentLocationTypeCode(LocationType.PRE);
    shipmentLocation.setDisplayedName("DisplayedName");
    shipmentLocation.setEventDateTime(dateTimeOffset);

    ConfirmedEquipmentTO confirmedEquipment = new ConfirmedEquipmentTO();
    confirmedEquipment.setConfirmedEquipmentUnits(12);
    confirmedEquipment.setConfirmedEquipmentSizetype("WHAT");

    ChargeTO charge = new ChargeTO();
    charge.setChargeTypeCode("ChargeTypeCode");
    charge.setCalculationBasis("CalculationBasis");
    charge.setCurrencyAmount(12.12);
    charge.setCurrencyCode("ABC");
    charge.setQuantity(12.12);
    charge.setPaymentTermCode(PaymentTerm.PRE);
    charge.setUnitPrice(12.12);

    CarrierClauseTO carrierClause = new CarrierClauseTO();
    carrierClause.setClauseContent("ClauseContent");

    shipmentTO = new ShipmentTO();
    shipmentTO.setCarrierBookingReference(carrierBookingReferenceID);
    shipmentTO.setTermsAndConditions(termsAndConditions);
    shipmentTO.setTransports(List.of(transport));
    shipmentTO.setShipmentCutOffTimes(List.of(shipmentCutOffTime));
    shipmentTO.setShipmentLocations(List.of(shipmentLocation));
    shipmentTO.setConfirmedEquipments(List.of(confirmedEquipment));
    shipmentTO.setCharges(List.of(charge));
    shipmentTO.setCarrierClauses(List.of(carrierClause));
  }

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus200 =
      (exchange) -> exchange.expectStatus().isOk();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus204 =
      (exchange) -> exchange.expectStatus().isNoContent();

  private final Function<WebTestClient.ResponseSpec, WebTestClient.ResponseSpec> checkStatus400 =
      (exchange) -> exchange.expectStatus().isBadRequest();

  @Test
  @DisplayName(
      "Get shipments should return valid list of shipment summaries for valid request.")
  void shipmentShouldReturnListOfShipments() {

    Mockito.when(
            bookingService.getShipmentByCarrierBookingReference(
                shipmentTO.getCarrierBookingReference()))
        .thenReturn(Mono.just(shipmentTO));

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(
                CONFIRMED_BOOKING_ENDPOINT
                    + "/"
                    + shipmentTO.getCarrierBookingReference())
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus200.andThen(checkBookingResponseJsonSchema).apply(exchange);
  }

  @Test
  @DisplayName("POST booking should return 400 for invalid request.")
  void postBookingsShouldReturn400ForInValidBookingRequest() {

    WebTestClient.ResponseSpec exchange =
        webTestClient
            .get()
            .uri(CONFIRMED_BOOKING_ENDPOINT + "/" + "y".repeat(36))
            .accept(MediaType.APPLICATION_JSON)
            .exchange();

    checkStatus400.apply(exchange);
  }

  private final Function<WebTestClient.ResponseSpec, WebTestClient.BodyContentSpec>
      checkBookingResponseJsonSchema =
          (exchange) ->
              exchange
                  .expectBody()
                  .consumeWith(System.out::println)
                  .jsonPath("$.carrierBookingReference")
                  .hasJsonPath()
                  .jsonPath("$.termsAndConditions")
                  .hasJsonPath()
                  .jsonPath("$.carrierClauses.[0].clauseContent")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].transportPlanStage")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].modeOfTransport")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].loadLocation")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].dischargeLocation")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].vesselName")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].vesselIMONumber")
                  .hasJsonPath()
                  .jsonPath("$.transports.[0].carrierVoyageNumber")
                  .hasJsonPath()
                  .jsonPath("$.shipmentCutOffTimes.[0].cutOffDateTimeCode")
                  .hasJsonPath()
                  .jsonPath("$.shipmentLocations.[0].location")
                  .hasJsonPath()
                  .jsonPath("$.shipmentLocations.[0].shipmentLocationTypeCode")
                  .hasJsonPath()
                  .jsonPath("$.shipmentLocations.[0].displayedName")
                  .hasJsonPath()
                  .jsonPath("$.confirmedEquipments.[0].confirmedEquipmentUnits")
                  .hasJsonPath()
                  .jsonPath("$.confirmedEquipments.[0].confirmedEquipmentSizetype")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].chargeTypeCode")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].calculationBasis")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].currencyAmount")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].currencyCode")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].quantity")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].paymentTermCode")
                  .hasJsonPath()
                  .jsonPath("$.charges.[0].unitPrice")
                  .hasJsonPath();
}
