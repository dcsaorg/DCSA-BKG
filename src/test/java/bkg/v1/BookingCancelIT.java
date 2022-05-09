package bkg.v1;

import bkg.config.TestConfig;
import org.apache.http.HttpStatus;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingResponseTO;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.UUID;

import static bkg.config.TestConfig.BOOKING;
import static bkg.config.TestConfig.jsonSchemaValidator;
import static bkg.config.TestUtil.loadFileAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingCancelIT {

  private static final String VALID_BOOKING = loadFileAsString("ValidBooking.json");
  private final String CANCEL_BOOKING_REQUEST_BODY =
      loadFileAsString("cancelBookingRequestBody.json");
  private static final String BOOKING_RESPONSE_SCHEMA = "bookingResponse";
  private static BookingResponseTO bookingResponse;

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
    bookingResponse = postValidBooking();
  }

  @Order(1) // THIS IS ORDER BASED;
  @Test
  void cancelBookingWithValidDocumentStatus() {

    given()
        .contentType("application/json")
        .body(CANCEL_BOOKING_REQUEST_BODY)
        .patch(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "carrierBookingRequestReference",
            equalTo(bookingResponse.getCarrierBookingRequestReference()))
        .body("documentStatus", equalTo(ShipmentEventTypeCode.CANC.toString()))
        .body("bookingRequestCreatedDateTime", notNullValue())
        .body("bookingRequestUpdatedDateTime", notNullValue())
        .body(jsonSchemaValidator(BOOKING_RESPONSE_SCHEMA));
  }

  // THIS IS ORDER BASED; DEPENDENT ON cancelBookingWithValidDocumentStatus to cancel booking first
  @Order(2)
  @Test
  void cancelBookingWithInvalidDocumentStatus() {
    assert bookingResponse.getCarrierBookingRequestReference() != null;

    given()
        .contentType("application/json")
        .body(CANCEL_BOOKING_REQUEST_BODY)
        .patch(BOOKING + "/" + bookingResponse.getCarrierBookingRequestReference())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            containsString("Cannot Cancel Booking that is not in status RECE, PENU, CONF or PENC"));
  }

  @Test
  void cancelBookingInvalidCarrierBookingReference() {

    given()
        .contentType("application/json")
        .body(CANCEL_BOOKING_REQUEST_BODY)
        .patch(BOOKING + "/" + UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  private static BookingResponseTO postValidBooking() {
    return given()
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
        .body(jsonSchemaValidator(BOOKING_RESPONSE_SCHEMA))
        .extract()
        .body()
        .as(BookingResponseTO.class);
  }
}
