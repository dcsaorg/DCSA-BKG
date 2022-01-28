package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;

@Data
public class ShipmentSummaryTO {

  @Size(max = 35)
  private String carrierBookingReference;

  private String termsAndConditions;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime shipmentCreatedDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime shipmentUpdatedDateTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String carrierBookingRequestReference;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @EnumSubset(anyOf = BOOKING_DOCUMENT_STATUSES)
  private ShipmentEventTypeCode documentStatus;
}