package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.core.events.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
  @Mapping(source = "invoicePayableAt", target = "invoicePayableAt.id")
  @Mapping(source = "placeOfIssueID", target = "placeOfIssue.id")
  @Mapping(source = "communicationChannelCode", target = "communicationChannel")
  @Mapping(source = "updatedDateTime", target = "bookingRequestUpdatedDateTime")
  BookingTO bookingToDTO(Booking booking);

  @Mapping(source = "invoicePayableAt", target = "invoicePayableAt", ignore = true)
  @Mapping(source = "communicationChannel", target = "communicationChannelCode")
  @Mapping(source = "bookingRequestUpdatedDateTime", target = "updatedDateTime")
  Booking dtoToBooking(BookingTO bookingTO);
}
