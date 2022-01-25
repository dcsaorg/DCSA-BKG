package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.model.transferobjects.BookingResponseTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.exception.CreateException;
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
          @PathVariable @Size(max = 100) String carrierBookingRequestReference, @RequestBody BookingCancellationRequestTO bookingCancellationRequestTO) {
    if (!ShipmentEventTypeCode.CANC.equals(bookingCancellationRequestTO.getDocumentStatus())) {
      return Mono.error(new CreateException("documentStatus '" + bookingCancellationRequestTO.getDocumentStatus().getValue() + "' not equal to '" + ShipmentEventTypeCode.CANC));
    }
    return bookingService.cancelBookingByCarrierBookingReference(carrierBookingRequestReference, bookingCancellationRequestTO);
  }
}
