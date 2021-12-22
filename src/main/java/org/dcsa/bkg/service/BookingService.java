package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
  Flux<BookingSummaryTO> getBookingRequestSummaries(DocumentStatus documentStatus, Pageable pageable);

  Mono<BookingTO> createBooking(BookingTO bookingRequest);

  Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest);

  Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference);

  Mono<ShipmentTO> getShipmentByCarrierBookingReference(String carrierBookingReference);

  Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(String carrierBookingReference);

  Flux<ShipmentSummaryTO> getShipmentSummaries(DocumentStatus documentStatus, Pageable pageable);
}
