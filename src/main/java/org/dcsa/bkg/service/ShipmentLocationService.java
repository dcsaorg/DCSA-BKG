package org.dcsa.bkg.service;

import org.dcsa.bkg.model.transferobjects.ShipmentLocationTO;
import org.dcsa.core.service.ExtendedBaseService;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentLocationService extends ExtendedBaseService<ShipmentLocationTO, UUID> {
    Flux<ShipmentLocationTO> findAllByShipmentID(UUID shipmentID);
    Flux<ShipmentLocationTO> findAllByBookingID(UUID bookingID);
}
