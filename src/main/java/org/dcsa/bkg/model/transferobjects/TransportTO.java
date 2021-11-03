package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.bkg.model.enums.TransportPlanStage;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.enums.DCSATransportType;
import org.dcsa.core.validator.ValidVesselIMONumber;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class TransportTO {

  @NotNull(message = "TransportPlanStage is required.")
  private TransportPlanStage transportPlanStage;

  private int transportPlanStageSequenceNumber;

  @NotNull(message = "LoadLocation is required.")
  private Location loadLocation;

  @NotNull(message = "DischargeLocation is required.")
  private Location dischargeLocation;

  @NotNull(message = "PlannedDepartureDate is required.")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate plannedDepartureDate;

  @NotNull(message = "PlannedArrivalDate is required.")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate plannedArrivalDate;

  private DCSATransportType modeOfTransport;

  @NotNull(message = "VesselName is required.")
  @Size(max = 35, message = "VesselName has a max size of 35.")
  private String vesselName;

  @ValidVesselIMONumber(allowNull = true, message = "VesselIMONumber is invalid.")
  private String vesselIMONumber;

  @NotNull(message = "CarrierVoyageNumber is required.")
  @Size(max = 50, message = "CarrierVoyageNumber has a max size of 50.")
  private String carrierVoyageNumber;

  private Boolean isUnderShippersResponsibility;
}
