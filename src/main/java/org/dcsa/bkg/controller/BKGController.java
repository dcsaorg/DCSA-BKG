package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/bookings",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGController {

  private final BookingService bookingService;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<BookingTO> createBooking(@Valid @RequestBody BookingTO bookingRequest) {
    return bookingService.createBooking(bookingRequest);
  }

  @PutMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<BookingTO> updateBookingByReference(
      @PathVariable String carrierBookingRequestReference,
      @Valid @RequestBody BookingTO bookingRequest) {
    // ToDo: adjust this when the IM is ready for booking
    return bookingService.updateBookingByReferenceCarrierBookingRequestReference(
        carrierBookingRequestReference, bookingRequest);
  }

  @GetMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingTO> getBookingByReference(
      @PathVariable String carrierBookingRequestReference) {
    return bookingService.getBookingByCarrierBookingRequestReference(
        carrierBookingRequestReference);
  }

  // To avoid spelling confusion we just accept both spellings
  @PostMapping(path = "{carrierBookingRequestReference}/cancelation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> bookingCancelation(
    @PathVariable @Size(max = 35) String carrierBookingRequestReference) {
    return bookingCancellation(carrierBookingRequestReference);
  }

  @PostMapping(path = "{carrierBookingRequestReference}/cancellation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> bookingCancellation(
    @PathVariable @Size(max = 35) String carrierBookingRequestReference) {
    return bookingService.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);
  }
}
