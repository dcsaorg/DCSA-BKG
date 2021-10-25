package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Location;

import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BookingConfirmationTO {

  @Size(max = 35)
  private String carrierBookingReferenceID;

  private String termsAndConditions;

  private Location placeOfIssue;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
  private OffsetDateTime bookingRequestDateTime;

  private Booking booking;

  private List<TransportTO> transports;

  private List<ShipmentCutOffTimeTO> shipmentCutOffTimes;

  private List<ShipmentLocationTO> shipmentLocations;

  private List<ConfirmedEquipmentTO> confirmedEquipments;

  private List<ChargeTO> charges;

  private List<CarrierClauseTO> carrierClauses;
}
