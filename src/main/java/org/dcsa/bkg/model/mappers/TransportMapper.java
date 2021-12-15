package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.TransportTO;
import org.dcsa.core.events.model.Transport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransportMapper {
  TransportTO transportToDTO(Transport transport);
}
