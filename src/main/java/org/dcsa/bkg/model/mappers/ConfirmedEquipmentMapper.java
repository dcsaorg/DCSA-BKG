package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.ConfirmedEquipmentTO;
import org.dcsa.core.events.model.RequestedEquipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfirmedEquipmentMapper {
	@Mapping(source = "confirmedEquipmentType", target = "confirmedEquipmentSizeType")
	ConfirmedEquipmentTO requestedEquipmentToDto(RequestedEquipment requestedEquipment);
}
