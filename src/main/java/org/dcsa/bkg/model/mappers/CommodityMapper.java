package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.CommodityTO;
import org.dcsa.core.events.model.Commodity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommodityMapper {
    CommodityTO commodityToDTO(Commodity commodity);
}
