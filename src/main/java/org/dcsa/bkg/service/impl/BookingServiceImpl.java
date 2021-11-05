package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.service.impl.BaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BookingServiceImpl extends BaseServiceImpl<BookingSummaryTO, UUID> implements BookingService {

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }

  @Override
  public Mono<BookingTO> createBooking(BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(String carrierBookingRequestReference, BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingConfirmationTO> getBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Flux<BookingSummaryTO> findAll() {
    return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<BookingSummaryTO> findById(UUID id) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<BookingSummaryTO> create(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<BookingSummaryTO> update(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<Void> deleteById(UUID id) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Mono<Void> delete(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public UUID getIdOfEntity(BookingSummaryTO entity) {
    return null;
  }
}
