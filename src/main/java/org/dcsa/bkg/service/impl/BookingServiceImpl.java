package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BookingServiceImpl implements BookingService {

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }
}
