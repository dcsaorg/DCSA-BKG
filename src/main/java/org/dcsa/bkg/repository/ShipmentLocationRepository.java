package org.dcsa.bkg.repository;

import org.dcsa.bkg.model.transferobjects.ShipmentLocationTO;
import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.core.repository.InsertAddonRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ShipmentLocationRepository extends ExtendedRepository<ShipmentLocationTO, UUID>, InsertAddonRepository<ShipmentLocationTO> {
    @Query("SELECT shipment_location.* FROM shipment_location"
            + " JOIN shipment ON (shipment_location.shipment_id=shipment.id)"
            + " WHERE shipment.carrier_booking_reference = :carrierBookingReference")
    Flux<ShipmentLocationTO> findAllByCarrierBookingReference(String carrierBookingReference);

    Flux<ShipmentLocationTO> findAllByShipmentID(UUID shipmentID);

    Flux<ShipmentLocationTO> findAllByBookingID(UUID shipmentID);
}
