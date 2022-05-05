package bkg.v1;

import bkg.config.TestConfig;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static bkg.config.TestConfig.BOOKING_SUMMARIES_ENDPOINT;
import static bkg.config.TestConfig.jsonSchemaValidator;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public class BookingSummaries2IT {

  @BeforeAll
  static void configs() throws IOException {
    TestConfig.init();
  }

  @Test
  void getAllBookingSummaries() {
    given()
        .contentType("application/json")
        .get(BOOKING_SUMMARIES_ENDPOINT)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", greaterThanOrEqualTo(5))
        .body(jsonSchemaValidator("bookingSummary"));
  }

  @Test
  void noBookingSummary() {
    given()
        .contentType("application/json")
        .queryParam("documentStatus", "CMPL") // Does not exist in test-data
        .get(BOOKING_SUMMARIES_ENDPOINT)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("size()", is(0));
  }

  @Test
  void filterBookingSummariesByDocumentStatus() {
    List<String> allDocumentStatusesInTestData =
        given()
            .contentType("application/json")
            .get(BOOKING_SUMMARIES_ENDPOINT)
            .body()
            .jsonPath()
            .getList("documentStatus");

    assert (!allDocumentStatusesInTestData.isEmpty());

    for (String DocumentStatus : allDocumentStatusesInTestData) {
      given()
          .contentType("application/json")
          .queryParam("documentStatus", DocumentStatus)
          .get(BOOKING_SUMMARIES_ENDPOINT)
          .then()
          .assertThat()
          .statusCode(HttpStatus.SC_OK)
          .body(
              "size()",
              greaterThanOrEqualTo(
                  1)) // At least one booking summary exists for each documentStatus
          .body(jsonSchemaValidator("bookingSummary"));
    }
  }

  @Test
  void filterByDocumentStatusError() {

    given()
        .contentType("application/json")
        .queryParam("documentStatus", "null")
        .get(BOOKING_SUMMARIES_ENDPOINT)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(
            "size()",
            greaterThanOrEqualTo(1)) // At least one booking summary exists for each documentStatus
        .body(jsonSchemaValidator("error"));
  }
}
