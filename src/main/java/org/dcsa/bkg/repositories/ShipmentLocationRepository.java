package org.dcsa.bkg.repositories;

import org.dcsa.bkg.model.transferobjects.ShipmentLocationTO;
import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocationTO, UUID> {
    Flux<ShipmentLocationTO> findAllByShipmentID(UUID shipmentID);
    Flux<ShipmentLocationTO> findAllByBookingID(UUID bookingID);
}
