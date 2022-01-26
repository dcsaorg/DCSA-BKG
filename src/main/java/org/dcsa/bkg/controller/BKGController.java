package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.model.transferobjects.BookingResponseTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.model.validators.DocumentPartyTOValidator;
import org.dcsa.bkg.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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

  @InitBinder
  private void initBinder(WebDataBinder dataBinder) {
    dataBinder.addValidators(new DocumentPartyTOValidator());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<BookingResponseTO> createBooking(@Valid @RequestBody BookingTO bookingRequest) {
    return bookingService.createBooking(bookingRequest);
  }

  @PutMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingResponseTO> updateBookingByReference(
      @PathVariable @Size(max = 100) String carrierBookingRequestReference,
      @Valid @RequestBody BookingTO bookingRequest) {
    return bookingService.updateBookingByReferenceCarrierBookingRequestReference(
        carrierBookingRequestReference, bookingRequest);
  }

  @GetMapping("/{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingTO> getBookingByReference(
      @PathVariable @Size(max = 100) String carrierBookingRequestReference) {
    return bookingService.getBookingByCarrierBookingRequestReference(
        carrierBookingRequestReference);
  }

  @PatchMapping("{carrierBookingRequestReference}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<BookingResponseTO> bookingCancellation(
          @PathVariable @Size(max = 100) String carrierBookingRequestReference, @RequestBody @Valid BookingCancellationRequestTO bookingCancellationRequestTO) {
    return bookingService.cancelBookingByCarrierBookingReference(carrierBookingRequestReference, bookingCancellationRequestTO);
  }
}
