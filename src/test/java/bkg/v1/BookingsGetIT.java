package bkg.v1;

import bkg.config.TestConfig;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static bkg.config.TestConfig.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

public class BookingsGetIT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void geBooking() {
    given()
        .contentType("application/json")
        .get(BOOKING + "/ef223019-ff16-4870-be69-9dbaaaae9b11")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("carrierBookingRequestReference", equalTo("ef223019-ff16-4870-be69-9dbaaaae9b11"))
        .body("bookingRequestCreatedDateTime", notNullValue())
        .body("bookingRequestUpdatedDateTime", notNullValue())
        .body("cargoMovementTypeAtDestination", notNullValue())
        .body("cargoMovementTypeAtOrigin", notNullValue())
        .body("carrierBookingRequestReference", notNullValue())
        .body("deliveryTypeAtDestination", notNullValue())
        .body("documentStatus", equalTo("PENU"))
        .body("isEquipmentSubstitutionAllowed", notNullValue())
        .body("isExportDeclarationRequired", notNullValue())
        .body("isImportLicenseRequired", notNullValue())
        .body("isPartialLoadAllowed", notNullValue())
        .body("receiptTypeAtOrigin", notNullValue())
        .body("serviceContractReference", notNullValue())
        .body("submissionDateTime", notNullValue())
        .body("commodities", notNullValue())
        .body(jsonSchemaValidator("booking"));
  }

  @Test
  void getBookingInvalidCarrierBookingReference() {
    given()
        .contentType("application/json")
        .get(BOOKING + UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }
}
