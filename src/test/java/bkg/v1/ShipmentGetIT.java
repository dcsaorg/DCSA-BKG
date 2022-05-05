package bkg.v1;

import bkg.config.TestConfig;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static bkg.config.TestConfig.SHIPMENT;
import static bkg.config.TestConfig.jsonSchemaValidator;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ShipmentGetIT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void getShipment() {
    given()
        .contentType("application/json")
        .get(SHIPMENT + "C501576CD94F")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("carrierBookingReference", equalTo("C501576CD94F"))
        .body("shipmentCreatedDateTime", notNullValue())
        .body("booking", notNullValue())
        .body("transports", notNullValue())
        .body("booking.documentStatus", equalTo("CONF"))
        .body(jsonSchemaValidator("Shipment"));
  }

  @Test
  void getShipmentInvalidCarrierBookingReference() {
    given()
        .contentType("application/json")
        .get(SHIPMENT + UUID.randomUUID().toString().substring(0, 20))
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }
}
