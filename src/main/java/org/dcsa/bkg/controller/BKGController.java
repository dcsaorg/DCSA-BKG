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
    // ToDo: adjust this when the IM is ready for booking
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
    // ToDo: adjust this when the IM is ready for booking
    return bookingService.getBookingByCarrierBookingRequestReference(
        carrierBookingRequestReference);
  }
}
