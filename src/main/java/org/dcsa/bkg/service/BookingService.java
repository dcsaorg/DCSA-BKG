package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
  Flux<BookingSummaryTO> getBookingRequestSummaries();

  Mono<BookingConfirmationTO> getBookingByCarrierBookingReference(String carrierBookingReference);

  Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference);
}
