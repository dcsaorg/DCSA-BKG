package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.service.BKGService;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingResponseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
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

  private final BKGService bkgService;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<BookingResponseTO> createBooking(@Valid @RequestBody BookingTO bookingRequest) {
    return bkgService.createBooking(bookingRequest);
  }

  @PutMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingResponseTO> updateBookingByReference(
      @PathVariable @Size(max = 100) String carrierBookingRequestReference,
      @Valid @RequestBody BookingTO bookingRequest) {
    return bkgService.updateBookingByReferenceCarrierBookingRequestReference(
        carrierBookingRequestReference, bookingRequest);
  }

  @GetMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingTO> getBookingByReference(
      @PathVariable @Size(max = 100) String carrierBookingRequestReference) {
    return bkgService.getBookingByCarrierBookingRequestReference(carrierBookingRequestReference);
  }

  @PatchMapping("{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingResponseTO> bookingCancellation(
      @PathVariable @Size(max = 100) String carrierBookingRequestReference,
      @RequestBody @Valid BookingCancellationRequestTO bookingCancellationRequestTO) {
    return bkgService.cancelBookingByCarrierBookingReference(
        carrierBookingRequestReference, bookingCancellationRequestTO);
  }
}
