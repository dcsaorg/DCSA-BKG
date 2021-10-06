package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTo;
import reactor.core.publisher.Flux;

public interface BookingService {
  Flux<BookingSummaryTo> getBookingRequestSummaries();
}
