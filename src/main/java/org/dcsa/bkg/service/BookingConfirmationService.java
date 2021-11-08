package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingConfirmationService extends ExtendedBaseService<Shipment, UUID> {
  Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries();
}
