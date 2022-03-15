package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingResponseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface BKGService {
  Mono<Page<BookingSummaryTO>> getBookingRequestSummaries(
      ShipmentEventTypeCode documentStatus, Pageable pageable);

  Mono<BookingResponseTO> createBooking(BookingTO bookingRequest);

  Mono<BookingResponseTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest);

  Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference);

  Mono<ShipmentTO> getShipmentByCarrierBookingReference(String carrierBookingReference);

  Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(
      String carrierBookingReference, BookingCancellationRequestTO bookingCancellationRequestTO);

  Mono<Page<ShipmentSummaryTO>> getShipmentSummaries(
      ShipmentEventTypeCode documentStatus, Pageable pageable);
}
