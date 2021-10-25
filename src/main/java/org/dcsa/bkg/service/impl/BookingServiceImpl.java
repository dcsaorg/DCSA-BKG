package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingServiceImpl implements BookingService {

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }

  @Override
  public Mono<BookingConfirmationTO> getBooking(String carrierBookingReference) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> cancelBooking(String carrierBookingReference) {
    return Mono.empty();
  }
}
