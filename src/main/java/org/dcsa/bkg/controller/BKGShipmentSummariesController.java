package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.controller.util.Pagination;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.DOCUMENT_STATUSES;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/shipment-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGShipmentSummariesController {

  private final BookingService bookingService;

  @GetMapping
  public Flux<ShipmentSummaryTO> getBookingConfirmationSummaries(
      @RequestParam(value = "documentStatus", required = false) @EnumSubset(anyOf = DOCUMENT_STATUSES) ShipmentEventTypeCode documentStatus,
      @RequestParam(
              value = "limit",
              defaultValue = "${pagination.defaultPageSize}",
              required = false)
          @Min(1)
          int limit,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "sort", required = false) String[] sort,
      ServerHttpResponse response) {

    Pagination pagination = new Pagination(Sort.by(Sort.Direction.DESC, "shipmentCreatedDateTime"));
    PageRequest pageRequest = pagination.createPageRequest(limit, cursor, sort);

    return bookingService
        .getShipmentSummaries(documentStatus, pageRequest)
        .doOnNext(
            shipmentSummaryTOS ->
                response.getHeaders().addAll(pagination.setPaginationHeaders(shipmentSummaryTOS)))
        .flatMapMany(shipmentSummaryTOS -> Flux.fromIterable(shipmentSummaryTOS));
  }
}
