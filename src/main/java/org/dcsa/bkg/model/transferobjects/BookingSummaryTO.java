package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingSummaryTO {

  private UUID bookingAcknowledgementID;

  private ReceiptDeliveryType receiptTypeAtOrigin;

  private ReceiptDeliveryType deliveryTypeAtDestination;

  private CargoMovementType cargoMovementTypeAtOrigin;

  private CargoMovementType cargoMovementTypeAtDestination;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
  private OffsetDateTime bookingRequestDateTime;

  private String serviceContractReference;

  private PaymentTerm paymentTerm;

  private CargoGrossWeight cargoGrossWeightUnit;

  private boolean isPartialLoadAllowed;

  private boolean isExportDeclarationRequired;

  private String exportDeclarationReference;

  private boolean isImportLicenseRequired;

  private String importLicenseReference;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
  private OffsetDateTime submissionDateTime;

  private boolean isAMSACIFilingRequired;

  private boolean isDestinationFilingRequired;

  @JsonProperty("OTICarrierCode")
  private String otiCarrierCode;

  @JsonProperty("800SeriesCarrierCode")
  private String seriesCarrierCode;

  private String contractQuotationReference;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime expectedDepartureDate;

  private TransportDocumentTypeCode transportDocumentType;

  private String transportDocumentReference;

  private String bookingChannelReference;

  private IncoTerms incoTerms;

  private String communicationChannel;

  private boolean isEquipmentSubstitutionAllowed;
}
