package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.Location;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class BookingConfirmationSummaryTO {

    @Size(max = 35)
    private String carrierBookingReferenceID;

    private String termsAndConditions;

    private Location placeOfIssue;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private OffsetDateTime bookingRequestDateTime;

}
