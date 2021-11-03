package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.enums.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingSummaryTO {

  private UUID bookingAcknowledgementID;

  private ReceiptDeliveryType receiptTypeAtOrigin;

  private ReceiptDeliveryType deliveryTypeAtDestination;

  private CargoMovementType cargoMovementTypeAtOrigin;

  private CargoMovementType cargoMovementTypeAtDestination;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime bookingRequestDateTime;

  private String serviceContractReference;

  private PaymentTerm paymentTerm;

  private CargoGrossWeight cargoGrossWeightUnit;

  private boolean isPartialLoadAllowed;

  private boolean isExportDeclarationRequired;

  private String exportDeclarationReference;

  private boolean isImportLicenseRequired;

  private String importLicenseReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime submissionDateTime;

  private boolean isAMSACIFilingRequired;

  private boolean isDestinationFilingRequired;

  private String contractQuotationReference;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate expectedDepartureDate;

  private TransportDocumentTypeCode transportDocumentType;

  private String transportDocumentReference;

  private String bookingChannelReference;

  private IncoTerms incoTerms;

  private CommunicationChannel communicationChannel;

  private boolean isEquipmentSubstitutionAllowed;
}
