package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.model.ForeignKey;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.sql.Join;

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

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @Column("confirmation_datetime")
  private OffsetDateTime confirmationDateTime;
}