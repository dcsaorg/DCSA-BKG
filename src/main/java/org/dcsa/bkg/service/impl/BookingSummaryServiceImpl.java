package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.mappers.BookingSummaryMapper;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.service.impl.AsymmetricQueryServiceImpl;
import org.dcsa.skernel.repositority.VesselRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingSummaryServiceImpl
    extends AsymmetricQueryServiceImpl<BookingRepository, Booking, BookingSummaryTO, UUID> {
  private final BookingRepository bookingRepository;
  private final VesselRepository vesselRepository;
  private final BookingSummaryMapper mapper;

  @Override
  protected Flux<BookingSummaryTO> bulkMapDM2TO(Flux<Booking> bookingFlux) {
    return bookingFlux.concatMap(
        booking ->
            vesselRepository
                .findByIdOrEmpty(booking.getVesselId())
                .map(
                    vessel -> {
                      BookingSummaryTO bookingSummaryTO =
                          mapper.bookingSummaryTOFromBooking(booking);
                      bookingSummaryTO.setVesselIMONumber(vessel.getVesselIMONumber());
                      return bookingSummaryTO;
                    })
                .defaultIfEmpty(mapper.bookingSummaryTOFromBooking(booking)));
  }

  @Override
  protected Mono<BookingSummaryTO> mapDM2TO(Booking booking) {
    throw new UnsupportedOperationException(
        "Should not be called, since a summaries endpoint acts on a Flux of items. org.dcsa.bkg.service.impl.BookingSummaryServiceImpl.bulkMapDM2TO should be used.");
  }

  @Override
  protected BookingRepository getRepository() {
    return bookingRepository;
  }
}
