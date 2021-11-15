package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.core.events.model.enums.LocationType;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.model.ForeignKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.sql.Join;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Table("shipment_location")
public class ShipmentLocationTO {

  @Id
  @JsonIgnore
  @Column("id")
  private UUID id;

  @NotNull
  @Column("shipment_id")
  private UUID shipmentID;

  @NotNull
  @Column("booking_id")
  private UUID bookingID;

  @JsonIgnore
  @Transient
  @ForeignKey(
      into = "location",
      foreignFieldName = "id",
      viaJoinAlias = "location",
      joinType = Join.JoinType.LEFT_OUTER_JOIN)
  @Column("location_id")
  private String LocationID;

  @Transient
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
