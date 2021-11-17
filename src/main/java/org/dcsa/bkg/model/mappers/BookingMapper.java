package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.core.events.model.Booking;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {
  @Mapping(source = "invoicePayableAt", target = "invoicePayableAt.id")
  @Mapping(source = "placeOfIssueID", target = "placeOfIssue.id")
  BookingTO bookingToDTO(Booking booking);
}
