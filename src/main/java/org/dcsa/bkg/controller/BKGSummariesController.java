package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.impl.BookingSummaryServiceImpl;
import org.dcsa.core.controller.AsymmetricQueryController;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.extendedrequest.QueryFieldRestriction;
import org.dcsa.core.query.DBEntityAnalysis;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/booking-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGSummariesController
    extends AsymmetricQueryController<BookingSummaryServiceImpl, Booking, BookingSummaryTO, UUID> {

  private final ExtendedParameters extendedParameters;
  private final R2dbcDialect r2dbcDialect;
  private final BookingSummaryServiceImpl service;

  @Override
  protected BookingSummaryServiceImpl getService() {
    return service;
  }

  @GetMapping
  public Flux<BookingSummaryTO> getBookingRequestSummaries(
      @RequestParam(value = "documentStatus", required = false)
          @EnumSubset(anyOf = BOOKING_DOCUMENT_STATUSES)
          ShipmentEventTypeCode documentStatus,
      ServerHttpResponse response,
      ServerHttpRequest request) {
    return super.findAll(response, request);
  }

  @Override
  protected ExtendedRequest<Booking> newExtendedRequest() {
    return new ExtendedRequest<>(extendedParameters, r2dbcDialect, Booking.class) {
      @Override
      protected DBEntityAnalysis.DBEntityAnalysisBuilder<Booking> prepareDBEntityAnalysis() {
        return super.prepareDBEntityAnalysis()
          .registerRestrictionOnQueryField("documentStatus", QueryFieldRestriction.enumSubset(BOOKING_DOCUMENT_STATUSES));
      }
    };
  }
}
