package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
  Flux<BookingSummaryTO> getBookingRequestSummaries(String carrierBookingRequestReference, DocumentStatus documentStatus, Pageable pageable);

  Mono<BookingTO> createBooking(BookingTO bookingRequest);

  Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest);

  Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference);

  Mono<BookingConfirmationTO> getBookingByCarrierBookingReference(String carrierBookingReference);

  Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference);
}
