package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.bkg.model.enums.CutOffDateTimeCode;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class ShipmentCutOffTimeTO {

  @NotNull
  private CutOffDateTimeCode cutOffDateTimeCode;

  @NotNull
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
  private OffsetDateTime cutOffDateTime;
}
