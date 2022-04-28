package org.dcsa.bkg.controller;

import org.dcsa.bkg.service.BKGEventService;
import org.dcsa.core.events.controller.AbstractEventController;
import org.dcsa.core.events.model.Event;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventType;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.enums.TransportDocumentTypeCode;
import org.dcsa.core.events.util.ExtendedGenericEventRequest;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryFieldRestriction;
import org.dcsa.core.query.DBEntityAnalysis;
import org.dcsa.core.validator.ValidEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static org.dcsa.core.events.model.enums.DocumentTypeCode.BKG_DOCUMENT_TYPE_CODES;

@RestController
@Validated
public class BKGEventController extends AbstractEventController<BKGEventService, Event> {

  private final BKGEventService bkgEventService;

  public BKGEventController(@Qualifier("BKGEventServiceImpl") BKGEventService bkgEventService) {
    this.bkgEventService = bkgEventService;
  }

  @Override
  public BKGEventService getService() {
    return this.bkgEventService;
  }

  @Override
  protected ExtendedRequest<Event> newExtendedRequest() {
    return new ExtendedGenericEventRequest(extendedParameters, r2dbcDialect) {
      @Override
      protected DBEntityAnalysis.DBEntityAnalysisBuilder<Event> prepareDBEntityAnalysis() {
        return super.prepareDBEntityAnalysis()
          .registerRestrictionOnQueryField("eventType", QueryFieldRestriction.enumSubset(EventType.SHIPMENT.name()))
          .registerRestrictionOnQueryField("documentTypeCode", QueryFieldRestriction.enumSubset(BKG_DOCUMENT_TYPE_CODES));
      }
    };
  }

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public Flux<Event> findAll(
      @RequestParam(value = "shipmentEventTypeCode", required = false)
          @ValidEnum(clazz = ShipmentEventTypeCode.class)
          String shipmentEventTypeCode,
      @RequestParam(value = "documentTypeCode", required = false)
          @ValidEnum(clazz = DocumentTypeCode.class)
          String documentTypeCode,
      @RequestParam(value = "carrierBookingReference", required = false) @Size(max = 35)
          String carrierBookingReference,
      @RequestParam(value = "transportDocumentReference", required = false) @Size(max = 20)
          String transportDocumentReference,
      @RequestParam(value = "transportDocumentTypeCode", required = false)
          @ValidEnum(clazz = TransportDocumentTypeCode.class)
          String transportDocumentTypeCode,
      @RequestParam(value = "limit", defaultValue = "20", required = false) @Min(1) int limit,
      ServerHttpResponse response,
      ServerHttpRequest request) {
    return super.findAll(response, request);
  }
}
