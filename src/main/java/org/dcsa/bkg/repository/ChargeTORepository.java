package org.dcsa.bkg.repository;

import org.dcsa.bkg.model.transferobjects.ChargeTO;
import org.dcsa.core.repository.ExtendedRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ChargeTORepository extends ExtendedRepository<ChargeTO, UUID> {
    Flux<ChargeTO> findAllByShipmentID(UUID shipmentID);
}