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

  @NotNull(message = "Charge Type is required.")
  @Size(max = 20, message = "Charge Type has a max size of 20.")
  private String chargeType;

  @NotNull(message = "Currency Amount is required.")
  private Number currencyAmount;

  @NotNull(message = "Currency Code is required.")
  @Size(max = 3, message = "Currency Code has a max size of 3.")
  private String currencyCode;

  @NotNull(message = "Is Under Shippers Responsibility is required.")
  private PaymentTerm isUnderShippersResponsibility;

  @NotNull(message = "Calculation Basis is required.")
  @Size(max = 50, message = "Calculation Basis has a max size of 50.")
  private String calculationBasis;

  @NotNull(message = "Unit Price is required.")
  private Number unitPrice;

  @NotNull(message = "Quantity is required.")
  private Number quantity;
}
