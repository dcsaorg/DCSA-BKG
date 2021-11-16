package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.mappers.BookingSummaryMapping;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.VesselRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  private final BookingSummaryMapping bookingSummaryMapping;
  private final BookingRepository bookingRepository;
  private final VesselRepository vesselRepository;

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries(String carrierBookingRequestReference, DocumentStatus documentStatus, Pageable pageable) {
    Booking bookingRequest = new Booking();
    bookingRequest.setCarrierBookingRequestReference(carrierBookingRequestReference);
    bookingRequest.setDocumentStatus(documentStatus);

        Flux<Booking> queryResponse = bookingRepository.findAllOrderByBookingRequestDateTime(Example.of(bookingRequest), pageable);

    return queryResponse.flatMap(
        booking -> vesselRepository.findByIdOrEmpty(booking.getVesselId())
                .mapNotNull(
                    vessel -> {
                      BookingSummaryTO bookingSummaryTO =
                          bookingSummaryMapping.bookingSummaryTOFromBooking(booking);
                      bookingSummaryTO.setVesselIMONumber(vessel.getVesselIMONumber());
                      return bookingSummaryTO;
                    }).defaultIfEmpty(bookingSummaryMapping.bookingSummaryTOFromBooking(booking)));
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
  public Mono<BookingConfirmationTO> getBookingByCarrierBookingReference(
      String carrierBookingReference) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }
}
