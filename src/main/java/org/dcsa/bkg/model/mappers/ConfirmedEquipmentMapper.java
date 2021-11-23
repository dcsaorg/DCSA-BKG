package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.ConfirmedEquipmentTO;
import org.dcsa.core.events.model.RequestedEquipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfirmedEquipmentMapper {
	ConfirmedEquipmentTO requestedEquipmentToDto(RequestedEquipment requestedEquipment);
}
