package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.service.BookingConfirmationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BookingConfirmationServiceImpl implements BookingConfirmationService {
  @Override
  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries() {
    return Flux.empty();
  }
}
