package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.CarrierClauseTO;
import org.dcsa.core.events.model.CarrierClause;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarrierClauseMapper {
  CarrierClauseTO carrierClauseToDTO(CarrierClause carrierClause);
}
