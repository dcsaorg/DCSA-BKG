package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.TransportDocument;
import org.dcsa.core.events.model.enums.PaymentTerm;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Table("charge")
public class ChargeTO {

  @Column("shipment_id")
  private UUID shipmentID;

  @NotNull
  @Column("transport_document_reference")
  private TransportDocument transportDocument;

  @NotNull(message = "ChargeType is required.")
  @Size(max = 20, message = "ChargeType has a max size of 20.")
  @Column("charge_type_code")
  private String chargeType;

  @NotNull(message = "CurrencyAmount is required.")
  private Double currencyAmount;

  @NotNull(message = "CurrencyCode is required.")
  @Size(max = 3, message = "CurrencyCode has a max size of 3.")
  private String currencyCode;

  @NotNull(message = "PaymentTermCode is required.")
  private PaymentTerm paymentTermCode;

  @NotNull(message = "CalculationBasis is required.")
  @Size(max = 50, message = "CalculationBasis has a max size of 50.")
  private String calculationBasis;

  @NotNull(message = "UnitPrice is required.")
  private Double unitPrice;

  @NotNull(message = "Quantity is required.")
  private Double quantity;
}
