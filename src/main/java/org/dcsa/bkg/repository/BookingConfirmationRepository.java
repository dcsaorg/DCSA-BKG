package org.dcsa.bkg.repository;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.repository.ExtendedRepository;
import reactor.core.publisher.Mono;

public interface BookingConfirmationRepository
    extends ExtendedRepository<Shipment, String> {
    Mono<Shipment> findByCarrierBookingReferenceID(String carrierBookingReference);

}
