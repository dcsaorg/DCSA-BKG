package bkg.v1;

import bkg.config.TestConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static bkg.config.TestConfig.*;
import static bkg.config.TestUtil.jsonToMap;
import static bkg.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookingPostIT {

  private final String VALID_BOOKING = loadFileAsString("ValidBooking.json");

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void postValidBooking() {
    given()
        .contentType("application/json")
        .body(VALID_BOOKING)
        .post(BOOKING)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_ACCEPTED)
        .body("carrierBookingRequestReference", notNullValue())
        .body("documentStatus", equalTo(ShipmentEventTypeCode.RECE.toString()))
        .body("bookingRequestCreatedDateTime", notNullValue())
        .body("bookingRequestUpdatedDateTime", notNullValue())
        .body(jsonSchemaValidator("Booking"));
  }

  @Test
  void postValidBookingOnlyRequired() throws JsonProcessingException {
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
    map.put("expectedArrivalAtPlaceOfDeliveryStartDate", null);
    map.put("expectedArrivalAtPlaceOfDeliveryEndDate", null);
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
    System.out.println(objectMapper.writeValueAsString(map));

    given()
        .contentType("application/json")
        .body(map)
        .post(BOOKING)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_ACCEPTED)
        .body("carrierBookingRequestReference", notNullValue())
        .body("documentStatus", equalTo(ShipmentEventTypeCode.RECE.toString()))
        .body("bookingRequestCreatedDateTime", notNullValue())
        .body("bookingRequestUpdatedDateTime", notNullValue())
        .body(jsonSchemaValidator("Booking"));
  }

  @Test
  void postInvalidBookingExportDeclarationIsRequiredWithNullReference() {
    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("isExportDeclarationRequired", true);
    map.put("exportDeclarationReference", null);

    given()
        .contentType("application/json")
        .body(map)
        .post(BOOKING)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(containsString("exportDeclarationReference cannot be null if isExportDeclarationRequired is true."))
        .body(jsonSchemaValidator("Booking"));
  }

  @Test
  void postInvalidBookingImportLicenseIsRequiredWithNullReference() {
    Map<String, Object> map = jsonToMap(VALID_BOOKING);
    assert map != null;
    map.put("isImportLicenseRequired", true);
    map.put("importLicenseReference", null);

    given()
        .contentType("application/json")
        .body(map)
        .post(BOOKING)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(containsString("importLicenseReference cannot be null if isImportLicenseRequired is true."))
        .body(jsonSchemaValidator("Booking"));
  }

  @Test
  void postInvalidBooking() {
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
        .post(BOOKING)
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
        .body(containsString("field 'commodities': rejected value [null]"))
        .body(jsonSchemaValidator("Booking"));
  }
}
