package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.DocumentStatus;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class BookingResponseTO {
  @NotNull private String carrierBookingRequestReference;

  @NotNull private DocumentStatus documentStatus;

  @NotNull private OffsetDateTime bookingRequestCreatedDateTime;

  @NotNull private OffsetDateTime bookingRequestUpdatedDateTime;
}
