package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

  private final ShipmentRepository shipmentRepository;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries() {
    return shipmentRepository.findAll().map(x -> {
      BookingConfirmationSummaryTO result = new BookingConfirmationSummaryTO();
      result.setCarrierBookingReferenceID(x.getCarrierBookingReferenceID());
      result.setConfirmationDateTime(x.getConfirmationDateTime());
      result.setTermsAndConditions(x.getTermsAndConditions());
      return result;
    });
  }

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

}
