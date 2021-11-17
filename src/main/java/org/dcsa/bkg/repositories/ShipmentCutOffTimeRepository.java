package org.dcsa.bkg.repositories;

import org.dcsa.bkg.model.transferobjects.ShipmentCutOffTimeTO;
import org.dcsa.core.repository.ExtendedRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentCutOffTimeRepository
    extends ExtendedRepository<ShipmentCutOffTimeTO, UUID> {
  Flux<ShipmentCutOffTimeTO> findAllByBookingID(UUID bookingID);
}
