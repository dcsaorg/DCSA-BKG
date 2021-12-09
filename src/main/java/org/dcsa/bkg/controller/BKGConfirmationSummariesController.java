package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
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
import javax.validation.constraints.Size;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/confirmed-booking-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGConfirmationSummariesController {

  private final BookingService bookingService;

  @GetMapping
  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries(
      @RequestParam(value = "carrierBookingReference", required = false) @Size(max = 35)
          String carrierBookingReference,
      @RequestParam(value = "documentStatus", required = false) DocumentStatus documentStatus,
      @RequestParam(value = "limit", defaultValue = "${pagination.defaultPageSize}", required = false) @Min(1) int limit,
      @RequestParam(value = "cursor", defaultValue = "0", required = false) String cursor) {
    return bookingService.getBookingConfirmationSummaries(carrierBookingReference, documentStatus, PageRequest.of(Integer.parseInt(cursor), limit));
  }
}
