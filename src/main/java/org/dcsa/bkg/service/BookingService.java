package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface BookingService {
  Mono<Page<BookingSummaryTO>> getBookingRequestSummaries(
      DocumentStatus documentStatus, Pageable pageable);

  Mono<BookingResponseTO> createBooking(BookingTO bookingRequest);

  Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest);

  Mono<BookingTO> getBookingByCarrierBookingRequestReference(String carrierBookingRequestReference);

  Mono<ShipmentTO> getShipmentByCarrierBookingReference(String carrierBookingReference);

  Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(String carrierBookingReference, BookingCancellationRequestTO bookingCancellationRequestTO);

  Mono<Page<ShipmentSummaryTO>> getShipmentSummaries(
      DocumentStatus documentStatus, Pageable pageable);

//  default Mono<BookingResponseTO> toBookingResponseTO(BookingTO bookingTO) {
//    System.out.println("Greetings from toBookingResponseTO!");
//    BookingResponseTO response = new BookingResponseTO();
//    response.setCarrierBookingRequestReference(bookingTO.getCarrierBookingRequestReference());
//    response.setDocumentStatus(bookingTO.getDocumentStatus());
//    response.setBookingRequestCreatedDateTime(bookingTO.getBookingRequestCreatedDateTime());
//    response.setBookingRequestUpdatedDateTime(bookingTO.getBookingRequestUpdatedDateTime());
//    return Mono.just(response);
//  }
}
