package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.bkg.model.enums.CutOffDateTimeCode;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class ShipmentCutOffTimeTO {

  @NotNull(message = "Cut Off Date Time Code is required.")
  private CutOffDateTimeCode cutOffDateTimeCode;

  @NotNull(message = "Cut Off Date Time is required.")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime cutOffDateTime;
}
