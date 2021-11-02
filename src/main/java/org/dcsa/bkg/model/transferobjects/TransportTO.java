package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.bkg.model.enums.TransportPlanStage;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.enums.DCSATransportType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class TransportTO {

  @NotNull(message = "Transport Plan Stage is required.")
  private TransportPlanStage transportPlanStage;

  private int transportPlanStageSequenceNumber;

  @NotNull(message = "Load Location is required.")
  private Location loadLocation;

  @NotNull(message = "Discharge Location is required.")
  private Location dischargeLocation;

  @NotNull(message = "Planned Departure Date is required.")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime plannedDepartureDate;

  @NotNull(message = "Planned Arrival Date is required.")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime plannedArrivalDate;

  private DCSATransportType modeOfTransport;

  @NotNull(message = "Vessel Name is required.")
  @Size(max = 35, message = "Vessel Name has a max size of 35.")
  private String vesselName;

  @NotNull(message = "Vessel IMO Number is required.")
  @Size(max = 7, message = "Vessel IMO Number has a max size of 7.")
  private String vesselIMONumber;

  @NotNull(message = "Carrier Voyage Number is required.")
  @Size(max = 50, message = "Carrier Voyage Number has a max size of 50.")
  private String carrierVoyageNumber;

  private Boolean isUnderShippersResponsibility;
}
