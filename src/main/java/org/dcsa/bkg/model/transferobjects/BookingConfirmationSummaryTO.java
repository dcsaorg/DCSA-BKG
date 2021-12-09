package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class BookingConfirmationSummaryTO {

  @Size(max = 35)
  @Column("carrier_booking_reference")
  private String carrierBookingReference;

  @Column("terms_and_conditions")
  private String termsAndConditions;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @Column("confirmation_datetime")
  private OffsetDateTime confirmationDateTime;
}