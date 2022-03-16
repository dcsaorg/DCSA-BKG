package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingResponseTO;
import org.dcsa.core.events.edocumentation.model.transferobject.BookingTO;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import reactor.core.publisher.Mono;

public interface BKGService {

  Mono<BookingResponseTO> createBooking(BookingTO bookingRequest);

  Mono<BookingResponseTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest);

  Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference);

  Mono<ShipmentTO> getShipmentByCarrierBookingReference(String carrierBookingReference);

  Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(
      String carrierBookingReference, BookingCancellationRequestTO bookingCancellationRequestTO);

}
