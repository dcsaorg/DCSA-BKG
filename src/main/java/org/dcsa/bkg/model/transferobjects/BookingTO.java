package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.validator.ValidVesselIMONumber;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BookingTO {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String carrierBookingRequestReference;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private DocumentStatus documentStatus;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime bookingRequestDateTime;

  @NotNull(message = "ReceiptDeliveryTypeAtOrigin is required.")
  private ReceiptDeliveryType receiptDeliveryTypeAtOrigin;

  @NotNull(message = "DeliveryTypeAtDestination is required.")
  private ReceiptDeliveryType deliveryTypeAtDestination;

  @NotNull(message = "CargoMovementTypeAtOrigin is required.")
  private CargoMovementType cargoMovementTypeAtOrigin;

  @NotNull(message = "CargoMovementTypeAtDestination is required.")
  private CargoMovementType cargoMovementTypeAtDestination;

  @NotBlank(message = "ServiceContractReference is required.")
  @Size(max = 30, message = "ServiceContractReference has a max size of 30.")
  private String serviceContractReference;

  private PaymentTerm paymentTerm;

  @JsonProperty("isPartialLoadAllowed")
  private boolean isPartialLoadAllowed;

  @JsonProperty("isExportDeclarationRequired")
  private boolean isExportDeclarationRequired;

  private String exportDeclarationReference;

  @JsonProperty("isImportLicenseRequired")
  private boolean isImportLicenseRequired;

  @Size(max = 35, message = "ImportLicenseReference has a max size of 35.")
  private String importLicenseReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime submissionDateTime;

  @JsonProperty("isAMSACIFilingRequired")
  private boolean isAMSACIFilingRequired;

  @JsonProperty("isDestinationFilingRequired")
  private boolean isDestinationFilingRequired;

  @Size(max = 35, message = "ContractQuotationReference has a max size of 35.")
  private String contractQuotationReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime expectedDepartureDate;

  private TransportDocumentTypeCode transportDocumentType;

  @Size(max = 20, message = "TransportDocumentReference has a max size of 20.")
  private String transportDocumentReference;

  @Size(max = 20, message = "BookingChannelReference has a max size of 20.")
  private String bookingChannelReference;

  private IncoTerms incoTerms;

  private LocationTO invoicePayableAt;

  @NotNull(message = "CommunicationChannel is required.")
  private CommunicationChannel communicationChannel;

  @JsonProperty("isEquipmentSubstitutionAllowed")
  private boolean isEquipmentSubstitutionAllowed;

  @Size(max = 35, message = "VesselName has a max size of 35.")
  private String vesselName;

  @ValidVesselIMONumber(allowNull = true, message = "VesselIMONumber is invalid.")
  private String vesselIMONumber;

  @Size(max = 50, message = "CarrierVoyageNumber has a max size of 50.")
  private String carrierVoyageNumber;

  @Valid
  @NotEmpty(message = "Commodities are required.")
  private List<CommodityTO> commodities;

  @Valid private List<ValueAddedServiceRequestTO> valueAddedServiceRequests;

  @Valid private List<ReferenceTO> references;

  @Valid private List<RequestedEquipmentTO> requestedEquipments;

  @Valid private List<DocumentPartyTO> documentParties;

  @Valid private List<ShipmentLocationTO> shipmentLocations;
}
