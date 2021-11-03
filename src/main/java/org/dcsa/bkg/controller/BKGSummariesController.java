package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/booking-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGSummariesController {

  private final BookingService bookingService;

  @GetMapping
  public Flux<BookingSummaryTO> getBookingRequestSummaries(
      @RequestParam(value = "bookingAcknowledgementID", required = false)
          String bookingAcknowledgementID,
      @RequestParam(value = "documentStatus", required = false) DocumentStatus documentStatus,
      @RequestParam(value = "limit", defaultValue = "100") @Min(1) int limit) {
    // ToDo: adjust this when the IM is ready for booking
    return bookingService.getBookingRequestSummaries();
  }
}
