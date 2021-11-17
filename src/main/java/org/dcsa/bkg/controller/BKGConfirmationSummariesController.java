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

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/booking-confirmation-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGConfirmationSummariesController {

  private final BookingService bookingService;

  @GetMapping
  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries(
      @RequestParam(value = "carrierBookingReferenceID", required = false)
          String carrierBookingReferenceID,
      @RequestParam(value = "documentStatus", required = false) DocumentStatus documentStatus,
      @RequestParam(value = "limit", defaultValue = "100") @Min(1) int limit,
      @RequestParam(value = "cursor", required = false) String cursor) {
    // ToDo: adjust this when the IM is ready for booking
    return bookingService.getBookingConfirmationSummaries(carrierBookingReferenceID, documentStatus, PageRequest.of(Integer.parseInt(cursor), limit));
  }
}
