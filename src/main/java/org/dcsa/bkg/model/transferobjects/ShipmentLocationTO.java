package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.enums.LocationType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class ShipmentLocationTO {

  @NotNull private Location location;

  @Size(max = 250)
  private String displayedName;

  @NotNull private LocationType locationType;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime eventDateTime;
}
