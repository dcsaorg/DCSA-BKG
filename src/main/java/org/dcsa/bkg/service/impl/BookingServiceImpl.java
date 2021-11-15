package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.repositories.BookingConfirmationRepository;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.bkg.service.ShipmentLocationService;
import org.dcsa.core.events.model.Shipment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

  private final ShipmentLocationService shipmentLocationService;
  private final BookingConfirmationRepository bookingConfirmationRepository;

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }

  @Override
  public Mono<BookingTO> createBooking(BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(
      String carrierBookingRequestReference) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingConfirmationTO> getBookingConfirmationByCarrierBookingReference(
      String carrierBookingReference) {
    BookingConfirmationTO bookingConfirmationTO = new BookingConfirmationTO();
    return bookingConfirmationRepository
        .findByCarrierBookingReference(carrierBookingReference)
        .flatMapMany(
            x -> {
              Shipment shipment = new Shipment();
              shipment.setShipmentID(x.getShipmentID());
              shipment.setCarrierID(x.getCarrierID());
              shipment.setBookingID(x.getBookingID());
              bookingConfirmationTO.setCarrierBookingReference(x.getCarrierBookingReference());
              bookingConfirmationTO.setTermsAndConditions(x.getTermsAndConditions());
              return shipmentLocationService.findAllByBookingID(shipment.getBookingID());
            })
        .collectList()
        .doOnNext(bookingConfirmationTO::setShipmentLocations)
        .thenReturn(bookingConfirmationTO);
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }
}
