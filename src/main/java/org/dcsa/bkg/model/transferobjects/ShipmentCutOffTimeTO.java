package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.dcsa.bkg.model.enums.CutOffDateTimeCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Table("shipment_cutoff_time")
public class ShipmentCutOffTimeTO {

  @Id
  @JsonIgnore
  @Column("booking_id")
  private UUID bookingID;

  @NotNull(message = "CutOffDateTimeCode is required.")
  @Column("cut_off_time_code")
  private CutOffDateTimeCode cutOffDateTimeCode;

  @NotNull(message = "CutOffDateTime is required.")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @Column("cut_off_time")
  private OffsetDateTime cutOffDateTime;
}
