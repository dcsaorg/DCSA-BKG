package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.DOCUMENT_STATUSES;

@Data
public class BookingResponseTO {
  @NotNull private String carrierBookingRequestReference;

  @EnumSubset(anyOf = DOCUMENT_STATUSES)
  @NotNull private ShipmentEventTypeCode documentStatus;

  @NotNull private OffsetDateTime bookingRequestCreatedDateTime;

  @NotNull private OffsetDateTime bookingRequestUpdatedDateTime;
}
