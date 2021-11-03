package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.transferobjects.LocationTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class BookingConfirmationTO {

  @NotNull(message = "CarrierBookingReference is required.")
  @Size(max = 35, message = "CarrierBookingReference has a max size of 35.")
  private String carrierBookingReference;

  private String termsAndConditions;

  private LocationTO placeOfIssue;

  private Booking booking;

  private List<TransportTO> transports;

  private List<ShipmentCutOffTimeTO> shipmentCutOffTimes;

  private List<ShipmentLocationTO> shipmentLocations;

  private List<ConfirmedEquipmentTO> confirmedEquipments;

  private List<ChargeTO> charges;

  private List<CarrierClauseTO> carrierClauses;
}
