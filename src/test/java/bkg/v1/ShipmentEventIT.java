package bkg.v1;

import bkg.config.TestConfig;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.BiConsumer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesRegex;
import static bkg.config.TestConfig.jsonSchemaValidator;

public class ShipmentEventIT {
  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void testGetAllEventsAndHeaders() {
    given()
      .contentType("application/json")
      .get("/v1/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .header("API-Version", equalTo("1.0.0"))
      .header("Current-Page", matchesRegex("^https?://.*/v1/events\\?cursor=[a-zA-Z\\d]*$"))
      // .header("Next-Page", matchesRegex("^https?://.*/v1/events\\?cursor=[a-zA-Z\\d]*$"))
      // .header("Last-Page", matchesRegex("^https?://.*/v1/events\\?cursor=[a-zA-Z\\d]*$"))
      .body("size()", greaterThanOrEqualTo(0))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body(jsonSchemaValidator("shipmentEvent"))
    ;
  }

  @Test
  void testGetAllEventsByShipmentEventTypeCode() {
    BiConsumer<String, Matcher<String>> runner = (s, m) ->
      given()
        .contentType("application/json")
        .queryParam("shipmentEventTypeCode", s)
        .get("/v1/events")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", greaterThanOrEqualTo(0))
        .body("eventType", everyItem(equalTo("SHIPMENT")))
        .body("eventClassifierCode", everyItem(equalTo("ACT")))
        .body("shipmentEventTypeCode", everyItem(m))
        .body(jsonSchemaValidator("shipmentEvent"))
      ;

    runner.accept("APPR,ISSU", anyOf(equalTo("APPR"), equalTo("ISSU")));
    runner.accept("APPR", equalTo("APPR"));
    runner.accept("ISSU", equalTo("ISSU"));
  }

  @Test
  void testGetAllEventsByDocumentTypeCode() {
    BiConsumer<String, Matcher<String>> runner = (s, m) ->
      given()
        .contentType("application/json")
        .queryParam("documentTypeCode", s)
        .get("/v1/events")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", greaterThanOrEqualTo(0))
        .body("eventType", everyItem(equalTo("SHIPMENT")))
        .body("eventClassifierCode", everyItem(equalTo("ACT")))
        .body("documentTypeCode", everyItem(m))
        .body(jsonSchemaValidator("shipmentEvent"))
      ;
    runner.accept("BKG,CBR", anyOf(equalTo("BKG"), equalTo("CBR")));
    runner.accept("BKG", equalTo("BKG"));
    runner.accept("CBR", equalTo("CBR"));
  }

  @Test
  void testGetAllEventsByCombinedQuery() {
    given()
      .contentType("application/json")
      .queryParam("documentTypeCode", "CBR")
      .queryParam("shipmentEventTypeCode", "VOID")
      .get("/v1/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("size()", equalTo(0))
      .body(jsonSchemaValidator("shipmentEvent"))
    ;
  }

  @Test
  void testGetAllEventsByCarrierBookingReference() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingReference", "cbr-b83765166707812c8ff4")
      .get("/v1/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes at least 3 shipment events related to the reference. But something adding additional
      // events.
      .body("size()", equalTo(2))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("BKG"), equalTo("CBR"))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.size()", equalTo(2))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'BKG' }.documentReferenceValue", everyItem(equalTo("cbr-b83765166707812c8ff4")))
      .body(jsonSchemaValidator("shipmentEvent"))
    ;
  }

  @Test
  void testGetAllEventsByCarrierBookingRequestReference() {
    given()
      .contentType("application/json")
      .queryParam("carrierBookingRequestReference", "cbrr-b83765166707812c8ff4")
      .get("/v1/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes at least 3 shipment events related to the reference. But something adding additional
      // events.
      .body("size()", equalTo(2))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("BKG"), equalTo("CBR"))))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'CBR' }.size()", equalTo(2))
      .body("documentReferences.flatten().findAll { it.documentReferenceType == 'CBR' }.documentReferenceValue", everyItem(equalTo("cbrr-b83765166707812c8ff4")))
      .body(jsonSchemaValidator("shipmentEvent"))
    ;
  }

  @Test
  void testEventCreatedDateTimeRange() {
    OffsetDateTime now = OffsetDateTime.now();
    String rangeStart = now.minusHours(1).toString();
    String rangeEnd = now.plusHours(1).toString();
    given()
      .contentType("application/json")
      .queryParam("eventCreatedDateTime:gte", rangeStart)
      .queryParam("eventCreatedDateTime:lt", rangeEnd)
      .get("/v1/events")
      .then()
      .assertThat()
      .statusCode(200)
      .contentType(ContentType.JSON)
      // The test data includes 3 shipment events for this case. Given the narrow date range, it seems acceptable to
      // validate an exact match.
      .body("size()", equalTo(3))
      .body("eventType", everyItem(equalTo("SHIPMENT")))
      .body("eventClassifierCode", everyItem(equalTo("ACT")))
      .body("documentTypeCode", everyItem(anyOf(equalTo("BKG"), equalTo("CBR"))))
      .body("eventCreatedDateTime", everyItem(
        asDateTime(
          allOf(
            greaterThanOrEqualTo(ZonedDateTime.parse(rangeStart)),
            lessThan(ZonedDateTime.parse(rangeEnd))
          ))))
      .body(jsonSchemaValidator("shipmentEvent"))
    ;
  }


  /**
   * Convert the input (assumed to be String) into a ZonedDateTime before chaining off to the next
   * match
   *
   * <p>The conversion will use {@link ZonedDateTime#parse(CharSequence)}. If the parsing fails, the
   * value is assumed not to match.
   *
   * @param dateTimeMatcher The matcher that should operator on a ZonedDateTime
   * @return The combined matcher
   */
  // Use ChronoZonedDateTime as bound to avoid fighting generics with lessThan that "reduces"
  // ZonedDateTime
  // to the ChronoZonedDateTime (via Comparable)
  private static <T extends ChronoZonedDateTime<?>> Matcher<T> asDateTime(
    Matcher<T> dateTimeMatcher) {
    return new DateTimeMatcher<>(dateTimeMatcher);
  }

  @RequiredArgsConstructor
  private static class DateTimeMatcher<T extends ChronoZonedDateTime<?>> extends BaseMatcher<T> {

    private final Matcher<T> matcher;

    @Override
    public boolean matches(Object actual) {
      ZonedDateTime dateTime;
      if (!(actual instanceof String)) {
        return false;
      }
      try {
        dateTime = ZonedDateTime.parse((String) actual);
      } catch (DateTimeParseException e) {
        return false;
      }
      return matcher.matches(dateTime);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("as datetime ").appendDescriptionOf(matcher);
    }
  }
}
