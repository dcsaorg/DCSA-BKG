package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Location;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class BookingConfirmationTO {

  @NotNull(message = "Carrier Booking Reference ID is required.")
  @Size(max = 35, message = "Carrier Booking Reference ID has a max size of 35.")
  private String carrierBookingReferenceID;

  private String termsAndConditions;

  private Location placeOfIssue;

  private Booking booking;

  private List<TransportTO> transports;

  private List<ShipmentCutOffTimeTO> shipmentCutOffTimes;

  private List<ShipmentLocationTO> shipmentLocations;

  private List<ConfirmedEquipmentTO> confirmedEquipments;

  private List<ChargeTO> charges;

  private List<CarrierClauseTO> carrierClauses;
}
