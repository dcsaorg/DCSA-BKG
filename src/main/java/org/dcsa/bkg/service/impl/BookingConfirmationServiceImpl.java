package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.repository.BKGConfirmationSummaryTORepository;
import org.dcsa.bkg.service.BookingConfirmationService;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookingConfirmationServiceImpl
    extends ExtendedBaseServiceImpl<
        BKGConfirmationSummaryTORepository, BookingConfirmationSummaryTO, UUID>
    implements BookingConfirmationService {

  private final BKGConfirmationSummaryTORepository bkgConfirmationSummaryTORepository;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries() {
    return findAll();
  }

  @Override
  public BKGConfirmationSummaryTORepository getRepository() {
    return bkgConfirmationSummaryTORepository;
  }
}
