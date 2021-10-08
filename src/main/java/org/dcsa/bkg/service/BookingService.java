package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import reactor.core.publisher.Flux;

public interface BookingService {
  Flux<BookingSummaryTO> getBookingRequestSummaries();
}
