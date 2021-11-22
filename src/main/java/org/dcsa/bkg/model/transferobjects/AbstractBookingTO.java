package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.validator.ValidVesselIMONumber;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public abstract class AbstractBookingTO {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  protected String carrierBookingRequestReference;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  protected DocumentStatus documentStatus;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime bookingRequestDateTime;

  @NotNull(message = "ReceiptTypeAtOrigin is required.")
  protected ReceiptDeliveryType receiptTypeAtOrigin;

  @NotNull(message = "DeliveryTypeAtDestination is required.")
  protected ReceiptDeliveryType deliveryTypeAtDestination;

  @NotNull(message = "CargoMovementTypeAtOrigin is required.")
  protected CargoMovementType cargoMovementTypeAtOrigin;

  @NotNull(message = "CargoMovementTypeAtDestination is required.")
  protected CargoMovementType cargoMovementTypeAtDestination;

  @NotBlank(message = "ServiceContractReference is required.")
  @Size(max = 30, message = "ServiceContractReference has a max size of 30.")
  protected String serviceContractReference;

  protected PaymentTerm paymentTermCode;

  protected Boolean isPartialLoadAllowed;

  protected Boolean isExportDeclarationRequired;

  protected String exportDeclarationReference;

  protected Boolean isImportLicenseRequired;

  @Size(max = 35, message = "ImportLicenseReference has a max size of 35.")
  protected String importLicenseReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected OffsetDateTime submissionDateTime;

  protected Boolean isAMSACIFilingRequired;

  protected Boolean isDestinationFilingRequired;

  @Size(max = 35, message = "ContractQuotationReference has a max size of 35.")
  protected String contractQuotationReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  protected LocalDate expectedDepartureDate;

  protected TransportDocumentTypeCode transportDocumentTypeCode;

  @Size(max = 20, message = "TransportDocumentReference has a max size of 20.")
  protected String transportDocumentReference;

  @Size(max = 20, message = "BookingChannelReference has a max size of 20.")
  protected String bookingChannelReference;

  protected IncoTerms incoTerms;

  @NotNull(message = "CommunicationChannel is required.")
  protected CommunicationChannel communicationChannel;

  protected Boolean isEquipmentSubstitutionAllowed;

  @Size(max = 35, message = "VesselName has a max size of 35.")
  protected String vesselName;

  @ValidVesselIMONumber(allowNull = true, message = "VesselIMONumber is invalid.")
  protected String vesselIMONumber;

  @Size(max = 50, message = "CarrierVoyageNumber has a max size of 50.")
  protected String carrierVoyageNumber;
}
