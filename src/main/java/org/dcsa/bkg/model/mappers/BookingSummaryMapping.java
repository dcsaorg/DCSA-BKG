package org.dcsa.bkg.model.mappers;


import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.core.events.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingSummaryMapping {


    @Mapping(target = "expectedDepartureDate", ignore = true)
    @Mapping(target = "invoicePayableAt", ignore = true)
    BookingSummaryTO bookingSummaryTOFromBooking(Booking booking);
}
