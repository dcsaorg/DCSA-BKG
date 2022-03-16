package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.model.ForeignKey;
import org.dcsa.core.validator.EnumSubset;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.sql.Join;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;

@Data
@NoArgsConstructor
public class ShipmentSummaryTO extends Shipment {

  @ForeignKey(foreignFieldName = "id", fromFieldName = "bookingID", joinType = Join.JoinType.JOIN)
  @Transient
  @JsonIgnore
  private Booking booking;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String getCarrierBookingRequestReference() {
    return booking.getCarrierBookingRequestReference();
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @EnumSubset(anyOf = BOOKING_DOCUMENT_STATUSES)
  public ShipmentEventTypeCode getDocumentStatus() {
    return booking.getDocumentStatus();
  }
}
