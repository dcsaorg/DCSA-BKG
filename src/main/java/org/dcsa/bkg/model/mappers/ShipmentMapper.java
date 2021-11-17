package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.ShipmentLocationTO;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.ShipmentLocation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    BookingConfirmationTO shipmentToDTO(Shipment shipment);
    ShipmentLocationTO shipmentLocationToDTO(ShipmentLocation shipmentLocation);
}
