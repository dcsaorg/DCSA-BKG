package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.repository.BKGConfirmationSummaryTORepository;
import org.dcsa.bkg.service.BookingConfirmationService;
import org.dcsa.core.events.service.LocationService;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookingConfirmationServiceImpl
    extends ExtendedBaseServiceImpl<
        BKGConfirmationSummaryTORepository, BookingConfirmationSummaryTO, UUID>
    implements BookingConfirmationService {

  private final BKGConfirmationSummaryTORepository bkgConfirmationSummaryTORepository;
  private final LocationService locationService;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries() {
    return findAll().concatMap(this::loadRelatedEntities);
  }

  private Mono<BookingConfirmationSummaryTO> loadRelatedEntities(
      BookingConfirmationSummaryTO bookingConfirmationSummaryTO) {
    if (bookingConfirmationSummaryTO.getPlaceOfIssueID() == null)
      return Mono.just(bookingConfirmationSummaryTO);
    return locationService
        .findById(bookingConfirmationSummaryTO.getPlaceOfIssueID())
        .doOnNext(bookingConfirmationSummaryTO::setPlaceOfIssue)
        .thenReturn(bookingConfirmationSummaryTO);
  }

  @Override
  public BKGConfirmationSummaryTORepository getRepository() {
    return bkgConfirmationSummaryTORepository;
  }
}
