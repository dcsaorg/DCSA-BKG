package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.bkg.model.enums.TransportPlanStage;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.ModeOfTransport;
import org.dcsa.core.events.model.enums.DCSATransportType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class TransportTO {

  @NotNull private TransportPlanStage transportPlanStage;

  private int transportPlanStageSequenceNumber;

  @NotNull private Location loadLocation;

  @NotNull private Location dischargeLocation;

  @NotNull private OffsetDateTime plannedDepartureDate;

  @NotNull private OffsetDateTime plannedArrivalDate;

  private DCSATransportType modeOfTransport;

  @NotNull
  @Size(max = 35)
  private String vesselName;

  @NotNull
  @Size(max = 7)
  private String vesselIMONumber;

  @NotNull
  @Size(max = 50)
  private String carrierVoyageNumber;

  private Boolean isUnderShippersResponsibility;
}
