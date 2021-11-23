package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.events.model.enums.CutOffDateTimeCode;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ShipmentCutOffTimeTO {

  @NotNull(message = "CutOffDateTimeCode is required.")
  private CutOffDateTimeCode cutOffDateTimeCode;

  @NotNull(message = "CutOffDateTime is required.")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime cutOffDateTime;
}
