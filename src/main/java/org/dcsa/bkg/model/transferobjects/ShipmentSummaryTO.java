package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.DocumentStatus;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class ShipmentSummaryTO {

  @Size(max = 35)
  private String carrierBookingReference;

  private String termsAndConditions;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime confirmationDateTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String carrierBookingRequestReference;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private DocumentStatus documentStatus;
}