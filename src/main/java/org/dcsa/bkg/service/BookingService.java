package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.model.transferobjects.ShipmentTO;
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

  Mono<ShipmentTO> getShipmentByCarrierBookingReference(String carrierBookingReference);

  Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(String carrierBookingReference);

  Flux<ShipmentSummaryTO> getShipmentSummaries(String carrierBookingReference, DocumentStatus documentStatus, Pageable pageable);
}
