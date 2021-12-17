package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Min;

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
      @RequestParam(value = "documentStatus", required = false) DocumentStatus documentStatus,
      @RequestParam(value = "limit", defaultValue = "${pagination.defaultPageSize}", required = false) @Min(1) int limit,
      @RequestParam(value = "cursor", defaultValue = "0", required = false) String cursor) {
    return bookingService.getShipmentSummaries(documentStatus, PageRequest.of(Integer.parseInt(cursor), limit));
  }
}
