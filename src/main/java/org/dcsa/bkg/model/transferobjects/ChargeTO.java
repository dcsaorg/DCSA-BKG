package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.bkg.model.enums.TransportPlanStage;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.ModeOfTransport;
import org.dcsa.core.events.model.enums.PaymentTerm;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ChargeTO {

  @NotNull
  @Size(max = 20)
  private String chargeType;

  @NotNull
  private Number currencyAmount;

  @NotNull
  @Size(max = 3)
  private String currencyCode;

  @NotNull
  private PaymentTerm isUnderShippersResponsibility;

  @NotNull
  @Size(max = 50)
  private String calculationBasis;

  @NotNull
  private Number unitPrice;

  @NotNull
  private Number quantity;
}
