package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.ChargeTO;
import org.dcsa.core.events.model.Charge;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeMapper {
    ChargeTO chargeToDTO(Charge charge);
}
