package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.events.model.transferobjects.LocationTO;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingTO extends AbstractBookingTO {

  private LocationTO invoicePayableAt;

  private LocationTO placeOfIssue;

  @Valid
  @NotEmpty(message = "The property commodities is required.")
  private List<CommodityTO> commodities;

  @Valid private List<ValueAddedServiceRequestTO> valueAddedServiceRequests;

  @Valid private List<ReferenceTO> references;

  @Valid private List<RequestedEquipmentTO> requestedEquipments;

  @Valid private List<DocumentPartyTO> documentParties;

  @Valid private List<ShipmentLocationTO> shipmentLocations;
}
