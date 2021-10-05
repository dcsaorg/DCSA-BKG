package org.dcsa.bkg.controller;

import org.dcsa.bkg.service.BKGEventService;
import org.dcsa.core.events.model.Event;
import org.dcsa.core.events.model.Reference;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.exception.handler.GlobalExceptionHandler;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.security.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@DisplayName("Tests for BKG Event Controller")
@ActiveProfiles("test")
@WebFluxTest(controllers = {BKGEventController.class})
@Import(value = {GlobalExceptionHandler.class, SecurityConfig.class})
class BKGEventControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean
  @Qualifier("BKGEventServiceImpl")
  BKGEventService eventService;

  @MockBean ExtendedParameters extendedParameters;

  @MockBean R2dbcDialect r2dbcDialect;

  @MockBean ExtendedRequest extendedRequest;

  private Event event;
  private ShipmentEvent shipmentEvent;

  @BeforeEach
  void init() {
    event = new Event();
    event.setEventID(UUID.randomUUID());
    event.setEventType(EventType.SHIPMENT);
    event.setEventClassifierCode(EventClassifierCode.PLN);
    event.setEventDateTime(OffsetDateTime.now());
    event.setEventCreatedDateTime(OffsetDateTime.now());
    event.setCarrierBookingReference("DUMMY");

    shipmentEvent = new ShipmentEvent();
    shipmentEvent.setEventID(UUID.fromString("5e51e72c-d872-11ea-811c-0f8f10a32ea1"));
    shipmentEvent.setEventType(EventType.SHIPMENT);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.PLN);
    shipmentEvent.setShipmentEventTypeCode(ShipmentEventTypeCode.CONF);
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.BKG);
    shipmentEvent.setDocumentID("ABC123123123");
    Reference reference = new Reference();
    reference.setReferenceType(ReferenceTypeCode.FF);
    reference.setReferenceValue("String");
    shipmentEvent.setReferences(List.of(reference));

    Mockito.when(extendedParameters.getSortParameterName()).thenReturn("sort");
    Mockito.when(extendedParameters.getPaginationPageSizeName()).thenReturn("limit");
    Mockito.when(extendedParameters.getPaginationCursorName()).thenReturn("cursor");
    Mockito.when(extendedParameters.getIndexCursorName()).thenReturn("|Offset|");
    Mockito.when(extendedParameters.getEnumSplit()).thenReturn(",");
    Mockito.when(extendedParameters.getQueryParameterAttributeSeparator()).thenReturn(",");
    Mockito.when(extendedParameters.getPaginationCurrentPageName()).thenReturn("Current-Page");
    Mockito.when(extendedParameters.getPaginationFirstPageName()).thenReturn("First-Page");
    Mockito.when(extendedParameters.getPaginationPreviousPageName()).thenReturn("Last-Page");
    Mockito.when(extendedParameters.getPaginationNextPageName()).thenReturn("Next-Page");
    Mockito.when(extendedParameters.getPaginationLastPageName()).thenReturn("Last-Page");

    Mockito.when(r2dbcDialect.getBindMarkersFactory())
        .thenReturn(BindMarkersFactory.anonymous("?"));
  }

  @Test
  @DisplayName("Creation of an event should throw not supported for any request.")
  void eventCreationShouldThrowForbiddenForAnyRequest() {
    // test to confirm that the endpoint is disabled.
    webTestClient
        .post()
        .uri("/events")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(event))
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectStatus()
        .value((s) -> Assertions.assertEquals(405, s));
  }

  @Test
  @DisplayName("Updating an event should throw not supported for any request.")
  void eventUpdatingShouldThrowForbiddenForAnyRequest() {
    // test to confirm that the endpoint is disabled.
    webTestClient
        .put()
        .uri("/events/{id}", event.getEventID())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(event))
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectStatus()
        .value((s) -> Assertions.assertEquals(405, s));
  }

  @Test
  @DisplayName("Deleting an event should throw not supported for any request.")
  void eventDeletingShouldThrowForbiddenForAnyRequest() {
    // test to confirm that the endpoint is disabled.
    webTestClient
        .delete()
        .uri("/events/{id}", event.getEventID().toString())
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectStatus()
        .value((s) -> Assertions.assertEquals(405, s));
  }

  @Test
  @DisplayName("Get events should throw bad request for incorrect shipmentEventTypeCode format.")
  void testEventsShouldFailForIncorrectShipmentEventTypeCode() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/events")
                    .queryParam("shipmentEventTypeCode", "ABCD,DUMMY")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get events should throw bad request for incorrect documentTypeCode format.")
  void testEventsShouldFailForIncorrectTransportDocumentReference() {
    webTestClient
        .get()
        .uri(
            uriBuilder -> uriBuilder.path("/events").queryParam("documentTypeCode", "ABCD").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get events should throw bad request for incorrect carrierBookingReference length.")
  void testEventsShouldFailForIncorrectCarrierBookingReference() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/events")
                    .queryParam(
                        "carrierBookingReference", "ABC709951ABC709951ABC709951ABC709951564")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName(
      "Get events should throw bad request for incorrect transportDocumentReference length.")
  void testEventsShouldFailForIncorrectTransportDocumentTypeCode() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/events")
                    .queryParam(
                        "transportDocumentReference", "ABC709951ABC709951ABC709951ABC609951564")
                    .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName(
      "Get events should throw bad request for incorrect transportDocumentTypeCode format.")
  void testEventsShouldFailForIncorrectTransportEventTypeCode() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path("/events").queryParam("transportDocumentTypeCode", "DUMMY").build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get events should throw bad request if limit is zero.")
  void testEventsShouldFailForIncorrectLimit() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/events").queryParam("limit", 0).build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get events should return list of shipment events for valid request.")
  void testEventsShouldReturnShipmentEvents() {

    Mockito.when(eventService.findAllExtended(Mockito.any())).thenReturn(Flux.just(shipmentEvent));

    webTestClient
        .get()
        .uri("/events")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.[0].eventID")
        .isEqualTo(shipmentEvent.getEventID().toString())
        .jsonPath("$.[0].eventType")
        .isEqualTo(shipmentEvent.getEventType().toString());
  }

  @Test
  @DisplayName("Get events/{id} should return a shipment events for valid request.")
  void testEventsWithIDShouldReturnShipmentEvent() {

    Mockito.when(eventService.findById(Mockito.any())).thenReturn(Mono.just(shipmentEvent));

    webTestClient
            .get()
            .uri("/events/{id}", shipmentEvent.getEventID())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.eventID")
            .isEqualTo(shipmentEvent.getEventID().toString())
            .jsonPath("$.eventType")
            .isEqualTo(shipmentEvent.getEventType().toString());
  }
}
