package bkg.v1;

import bkg.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.model.enums.CargoMovementType;
import org.dcsa.core.events.model.enums.ReceiptDeliveryType;
import org.dcsa.skernel.model.enums.FacilityCodeListProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static bkg.config.TestConfig.BOOKING;
import static bkg.config.TestConfig.jsonSchemaValidator;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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
        .body("cargoMovementTypeAtOrigin", equalTo(CargoMovementType.FCL.toString()))
        .body("cargoMovementTypeAtDestination", equalTo(CargoMovementType.LCL.toString()))
        .body("deliveryTypeAtDestination", equalTo(ReceiptDeliveryType.CY.toString()))
        .body("receiptTypeAtOrigin", equalTo(ReceiptDeliveryType.CY.toString()))
        .body("documentStatus", equalTo("PENU"))
        .body("isEquipmentSubstitutionAllowed", equalTo(true))
        .body("isExportDeclarationRequired", equalTo(true))
        .body("exportDeclarationReference", notNullValue())
        .body("isImportLicenseRequired", equalTo(true))
        .body("importLicenseReference", notNullValue())
        .body("isPartialLoadAllowed", equalTo(true))
        .body("serviceContractReference", equalTo("Test"))
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
