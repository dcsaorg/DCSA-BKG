package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Size;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/confirmed-bookings",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGConfirmedBookingsController {

  private final BookingService bookingService;

  @GetMapping(path = "{carrierBookingReference}")
  public Mono<BookingConfirmationTO> getBookingReference(
      @PathVariable @Size(max = 35) String carrierBookingReference) {
    // ToDo: adjust this when the IM is ready for booking
    return bookingService.getBookingByCarrierBookingReference(carrierBookingReference);
  }

  // To avoid spelling confusion we just accept both spellings
  @PostMapping(path = "{carrierBookingReference}/cancelation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> bookingCancelation(
      @PathVariable @Size(max = 35) String carrierBookingReference) {
    // ToDo: adjust this when the IM is ready for booking
    return bookingCancellation(carrierBookingReference);
  }

  @PostMapping(path = "{carrierBookingRequestReference}/cancellation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> bookingCancellation(
      @PathVariable @Size(max = 35) String carrierBookingRequestReference) {
    return bookingService.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);
  }
}
