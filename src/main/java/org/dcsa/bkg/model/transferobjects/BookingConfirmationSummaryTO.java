package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.Location;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@Table("shipment")
public class BookingConfirmationSummaryTO {

  @Size(max = 35)
  @Column("carrier_booking_reference")
  private String carrierBookingReferenceID;

  @Column("terms_and_conditions")
  private String termsAndConditions;

  @Column("place_of_issue")
  private Location placeOfIssue;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime bookingRequestDateTime;
}
