package org.dcsa.bkg.repositories;

import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.repository.ExtendedRepository;
import reactor.core.publisher.Mono;

public interface BookingConfirmationRepository
        extends ExtendedRepository<Shipment, String> {
    Mono<Shipment> findByCarrierBookingReference(String carrierBookingReference);

}