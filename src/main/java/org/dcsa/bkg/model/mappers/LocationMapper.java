package org.dcsa.bkg.model.mappers;

import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
  LocationTO locationToDTO(Location location);
}
