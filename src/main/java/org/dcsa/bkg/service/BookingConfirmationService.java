package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import reactor.core.publisher.Flux;

public interface BookingConfirmationService {
    Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries();
}
