package org.dcsa.bkg.model.transferobjects;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BookingTO extends AbstractBookingTO {

  @Valid
  @NotEmpty(message = "Commodities are required.")
  private List<CommodityTO> commodities;

  @Valid private List<ValueAddedServiceRequestTO> valueAddedServiceRequests;

  @Valid private List<ReferenceTO> references;

  @Valid private List<RequestedEquipmentTO> requestedEquipments;

  @Valid private List<DocumentPartyTO> documentParties;

  @Valid private List<ShipmentLocationTO> shipmentLocations;
}
