package org.dcsa.bkg.model.mappers;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.core.events.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingSummaryMapper {

  @Mapping(source = "communicationChannelCode", target = "communicationChannelCode")
  @Mapping(source = "updatedDateTime", target = "bookingRequestUpdatedDateTime")
  @Mapping(source = "bookingRequestDateTime", target = "bookingRequestCreatedDateTime")
  BookingSummaryTO bookingSummaryTOFromBooking(Booking booking);
}
