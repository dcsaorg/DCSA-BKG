package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.extendedRequest.ShipmentSummaryExtendedRequest;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.service.impl.ShipmentSummaryServiceImpl;
import org.dcsa.core.controller.QueryController;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.validator.EnumSubset;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/shipment-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGShipmentSummariesController
    extends QueryController<ShipmentSummaryServiceImpl, ShipmentSummaryTO, UUID> {

  private final ShipmentSummaryServiceImpl service;
  private final ExtendedParameters extendedParameters;
  private final R2dbcDialect r2dbcDialect;

  @Override
  protected ShipmentSummaryServiceImpl getService() {
    return service;
  }

  @GetMapping
  public Flux<ShipmentSummaryTO> getShipmentSummaries(
      @RequestParam(value = "documentStatus", required = false)
          @EnumSubset(anyOf = BOOKING_DOCUMENT_STATUSES)
          ShipmentEventTypeCode documentStatus,
      ServerHttpResponse response,
      ServerHttpRequest request) {
    return super.findAll(response, request);
  }

  @Override
  protected ExtendedRequest<ShipmentSummaryTO> newExtendedRequest() {
    return new ShipmentSummaryExtendedRequest(extendedParameters, r2dbcDialect);
  }
}
