package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.events.model.enums.LocationType;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ShipmentLocationTO {

  @NotNull
  @JsonIgnore
  @Column("shipment_id")
  private UUID shipmentID;

  @NotNull
  @JsonIgnore
  @Column("booking_id")
  private UUID bookingID;

  @NotNull
  @JsonIgnore
  @Column("location_id")
  private UUID locationID;

  @NotNull(message = "Location is required.")
  private LocationTO location;

  @Size(max = 250, message = "DisplayName has a max size of 250.")
  @Column("displayed_name")
  private String displayedName;

  @NotNull(message = "LocationType is required.")
  @Column("shipment_location_type_code")
  private LocationType locationType;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @Column("event_date_time")
  private OffsetDateTime eventDateTime;
}