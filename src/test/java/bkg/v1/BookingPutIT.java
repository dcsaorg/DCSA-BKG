package bkg.v1;

import bkg.config.TestConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingResponseTO;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static bkg.config.TestConfig.BOOKING;
import static bkg.config.TestConfig.jsonSchemaValidator;
import static bkg.config.TestUtil.jsonToMap;
import static bkg.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookingPutIT {

  private final String VALID_BOOKING = loadFileAsString("ValidBooking.json");

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void putValidBooking() {
    BookingResponseTO bookingResponse = postBooking();

    ValidatableResponse response =
        given()
            .contentType("application/json")
            .body(VALID_BOOKING)
            .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
            .then()
            .assertThat()
            .statusCode(HttpStatus.SC_OK);

    responseValidator.apply(response);
  }

  @Test
  void putValidBookingOnlyRequired() {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("paymentTermCode", null);
    map.put("isExportDeclarationRequired", false);
    map.put("exportDeclarationReference", null);
    map.put("isImportLicenseRequired", false);
    map.put("importLicenseReference", null);
    map.put("isAMSACIFilingRequired", null);
    map.put("isDestinationFilingRequired", null);
    map.put("contractQuotationReference", null);
    map.put("expectedDepartureDate", null);
    map.put("transportDocumentTypeCode", null);
    map.put("transportDocumentReference", null);
    map.put("bookingChannelReference", null);
    map.put("incoTerms", null);
    map.put("vesselName", null);
    map.put("vesselIMONumber", null);
    map.put("exportVoyageNumber", null);
    map.put("preCarriageModeOfTransportCode", null);
    map.put("invoicePayableAt", null);
    map.put("placeOfIssue", null);
    map.put("valueAddedServiceRequests", null);
    map.put("references", null);
    map.put("requestedEquipments", null);
    map.put("documentParties", null);
    map.put("shipmentLocations", null);

    ValidatableResponse response =
        given()
            .contentType("application/json")
            .body(map)
            .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
            .then()
            .assertThat()
            .statusCode(HttpStatus.SC_OK);

    responseValidator.apply(response);
  }

  @Test
  void putInvalidBookingWithDocumentStatus() throws JsonProcessingException {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("documentStatus", ShipmentEventTypeCode.PENU);
    System.out.println(objectMapper.writeValueAsString(map));

    given()
        .contentType("application/json")
        .body(map)
        .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            containsString(
                "The error is associated with the attribute BookingTO[\\\"documentStatus\\\"]."));
  }

  @Test
  void putInvalidBookingVessel() {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("vesselIMONumber", null);
    map.put("expectedArrivalAtPlaceOfDeliveryStartDate", null);
    map.put("expectedArrivalAtPlaceOfDeliveryEndDate", null);
    map.put("expectedDepartureDate", null);
    map.put("exportVoyageNumber", null);

    given()
        .contentType("application/json")
        .body(map)
        .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            containsString(
                "The attributes expectedArrivalAtPlaceOfDeliveryStartDate, expectedArrivalAtPlaceOfDeliveryEndDate, expectedDepartureDate and vesselIMONumber/exportVoyageNumber cannot all be null at the same time. These fields are conditional and require that at least one of them is not empty."));
  }

  @Test
  void putInvalidBookingExportDeclarationIsRequiredWithNullReference() {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("isExportDeclarationRequired", true);
    map.put("exportDeclarationReference", null);

    given()
        .contentType("application/json")
        .body(map)
        .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            containsString(
                "exportDeclarationReference cannot be null if isExportDeclarationRequired is true."));
  }

  @Test
  void putInvalidBookingImportLicenseIsRequiredWithNullReference() {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("isImportLicenseRequired", true);
    map.put("importLicenseReference", null);

    given()
        .contentType("application/json")
        .body(map)
        .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            containsString(
                "importLicenseReference cannot be null if isImportLicenseRequired is true."));
  }

  @Test
  void putInvalidBooking() {
    BookingResponseTO bookingResponse = postBooking();

    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("receiptTypeAtOrigin", null);
    map.put("deliveryTypeAtDestination", null);
    map.put("cargoMovementTypeAtOrigin", null);
    map.put("cargoMovementTypeAtDestination", null);
    map.put("serviceContractReference", null);
    map.put("isPartialLoadAllowed", null);
    map.put("isExportDeclarationRequired", null);
    map.put("submissionDateTime", null);
    map.put("isEquipmentSubstitutionAllowed", null);
    map.put("commodities", null);

    given()
        .contentType("application/json")
        .body(map)
        .put(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(containsString("field 'receiptTypeAtOrigin': rejected value [null]"))
        .body(containsString("field 'deliveryTypeAtDestination': rejected value [null]"))
        .body(containsString("field 'cargoMovementTypeAtOrigin': rejected value [null]"))
        .body(containsString("field 'cargoMovementTypeAtDestination': rejected value [null]"))
        .body(containsString("field 'serviceContractReference': rejected value [null]"))
        .body(containsString("field 'isPartialLoadAllowed': rejected value [null]"))
        .body(containsString("field 'isExportDeclarationRequired': rejected value [null]"))
        .body(containsString("field 'submissionDateTime': rejected value [null]"))
        .body(containsString("field 'isEquipmentSubstitutionAllowed': rejected value [null]"))
        .body(containsString("field 'commodities': rejected value [null]"));
  }

  private BookingResponseTO postBooking() {
    ValidatableResponse response =
        given()
            .contentType("application/json")
            .body(VALID_BOOKING)
            .post(BOOKING)
            .then()
            .assertThat()
            .statusCode(HttpStatus.SC_ACCEPTED);

    return responseValidator.apply(response).extract().body().as(BookingResponseTO.class);
  }

  private final Function<ValidatableResponse, ValidatableResponse> responseValidator =
      response ->
          response
              .body("carrierBookingRequestReference", notNullValue())
              .body("documentStatus", equalTo(ShipmentEventTypeCode.RECE.toString()))
              .body("bookingRequestCreatedDateTime", notNullValue())
              .body("bookingRequestUpdatedDateTime", notNullValue())
              .body(jsonSchemaValidator("bookingResponse"));
}
